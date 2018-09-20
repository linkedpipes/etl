package com.linkedpipes.etl.executor.monitor.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
class ExecutorUpdater {

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecutorUpdater.class);

    private final ExecutorEventListener listener;

    private final ExecutorRestClient restClient;

    private final ExecutionSource executions;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ExecutorUpdater(
            ExecutorEventListener listener, ExecutorRestClient restClient,
            ExecutionSource executions) {
        this.listener = listener;
        this.restClient = restClient;
        this.executions = executions;
    }

    public void update(Executor executor) {
        String response;
        try {
            response = this.restClient.check(executor);
            executor.setAlive(true);
        } catch (Exception ex) {
            this.onHttpCheckFail(executor, ex);
            return;
        }

        if (response == null) {
            this.onExecutorIsNotExecuting(executor);
            return;
        }

        try {
            this.updateFromStream(executor, response);
        } catch (MonitorException ex) {
            if (executor.getExecution() == null) {
                return;
            }
            // There was an issued with update, detach the executor.
            this.listener.onDetachExecutor(executor.getExecution());
            executor.setExecution(null);
        }
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
        this.listener.onUnresponsiveExecutor(executor.getExecution());
    }

    private void onExecutorIsNotExecuting(Executor executor) {
        if (executor.getExecution() == null) {
            return;
        }
        // We have execution assigned to this executor, but now it is not
        // executing anything. We need to update from disk as the
        // execution might have been finished in a meantime.
        this.listener.onDetachExecutor(executor.getExecution());
        executor.setExecution(null);
    }

    private void updateFromStream(Executor executor, String body)
            throws MonitorException {
        JsonNode overview = toJson(body);
        Execution execution = executor.getExecution();
        if (execution == null) {
            execution = this.executions.getExecution(overview);
            if (execution == null) {
                LOG.info("Executor is running unknown execution:\n{}", body);
                return;
            }
            executor.setExecution(execution);
            this.listener.onAttachExecutor(executor.getExecution(), executor);
        }
        this.listener.onOverview(execution, overview);
    }

    private JsonNode toJson(String body) throws MonitorException {
        try {
            return mapper.readTree(body);
        } catch (IOException ex) {
            throw new MonitorException("Can't read overview JSON.", ex);
        }
    }

}
