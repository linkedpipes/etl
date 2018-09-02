package com.linkedpipes.etl.executor.monitor.executor;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

class HttpCheckExecutor {

    private static final Logger LOG =
            LoggerFactory.getLogger(HttpCheckExecutor.class);

    private final ExecutionFacade executionFacade;

    private final RestTemplate restTemplate = new RestTemplate();

    public HttpCheckExecutor(ExecutionFacade executionFacade) {
        this.executionFacade = executionFacade;
    }

    public void check(Executor executor) throws MonitorException {
        String response;
        try {
            response = this.makeHttpCheck(executor);
        } catch (Exception ex) {
            this.onHttpCheckFail(executor, ex);
            return;
        }
        executor.setLastCheck(new Date());
        if (response == null) {
            this.onExecutorIsNotExecuting(executor);
            return;
        }
        InputStream stream = new ByteArrayInputStream(
                response.getBytes(StandardCharsets.UTF_8));

        try {
            this.updateFromStream(executor, stream);
        } catch (MonitorException ex) {
            if (executor.getExecution() == null) {
                return;
            }
            // There was an issued with update, detach the executor.
            this.executionFacade.onDetachExecutor(executor.getExecution());
            executor.setExecution(null);
        }
    }

    private String makeHttpCheck(Executor executor) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Accept", "application/json");
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    executor.getAddress() + "/api/v1/executions/overview",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 404) {
                // There is no execution running that is fine.
                response = ResponseEntity.accepted().body(null);
            } else {
                throw ex;
            }
        }
        executor.setAlive(true);
        return response.getBody();
    }

    private void onHttpCheckFail(Executor executor, Exception ex) {
        if (executor.isAlive()) {
            // Print error only if we lost the connection for the first time.
            LOG.error("Can't connect to: {}", executor.getAddress(), ex);
        }
        executor.setAlive(false);
        if (executor.getExecution() == null) {
            return;
        }
        this.executionFacade.onUnresponsiveExecutor(executor.getExecution());
    }

    private void onExecutorIsNotExecuting(Executor executor) {
        if (executor.getExecution() == null) {
            // Is not executing anything.
            return;
        }
        // We have execution assigned to this executor, but now it is not
        // executing anything. We need to check from disk as the execution
        // might have been finished in a meantime.
        try {
            this.executionFacade.onDetachExecutor(executor.getExecution());
        } catch (MonitorException ex) {
            LOG.warn("Can't load from file before detach. {}",
                    executor.getExecution().getId());
        } finally {
            executor.setExecution(null);
        }
    }

    private void updateFromStream(Executor executor, InputStream stream)
            throws MonitorException {
        if (executor.getExecution() == null) {
            Execution exec = this.executionFacade.discoverExecution(stream);
            executor.setExecution(exec);
            this.executionFacade.onAttachExecutor(
                    executor.getExecution(), executor);
        } else {
            this.executionFacade.update(executor.getExecution(), stream);
        }
    }

}
