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
class CheckExecutor {

    private static final Logger LOG =
            LoggerFactory.getLogger(CheckExecutor.class);

    private final ExecutorEventListener listener;

    private final ExecutorRestClient restClient;

    private final ExecutionSource executions;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public CheckExecutor(
            ExecutorEventListener listener,
            ExecutorRestClient restClient,
            ExecutionSource executions) {
        this.listener = listener;
        this.restClient = restClient;
        this.executions = executions;
    }

    public void check(Executor executor) {
        String response;
        try {
            response = restClient.check(executor);
            if (!executor.isAlive()) {
                onExecutorIsAlive(executor);
            }
        } catch (Exception ex) {
            onHttpCheckFail(executor, ex);
            return;
        }
        if (response == null) {
            onExecutorWithoutExecution(executor);
            return;
        }
        JsonNode overview;
        try {
            overview = toJson(response);
        } catch (MonitorException ex) {
            LOG.error("Invalid response from executor: {}\n{}",
                    executor.getAddress(), response, ex);
            return;
        }
        Execution execution;
        try {
            execution = getExecution(executor, overview);
        } catch (MonitorException ex) {
            LOG.error("Can't get execution for: {}\n{}",
                    executor.getAddress(), response, ex);
            return;
        }
        reportExecutorHasExecution(executor, execution);
        if (execution == null) {
            LOG.error("Executor ({}) is running unknown execution.\n{}",
                    executor.getAddress(), response);
            return;
        }
        updateFromOverview(execution, overview);
    }

    private void onExecutorIsAlive(Executor executor) {
        executor.setAlive(true);
    }

    private void onHttpCheckFail(Executor executor, Exception ex) {
        if (!executor.isAlive()) {
            // Ignore as we know it is not alive.
            return;
        }
        executor.setAlive(false);
        listener.onExecutorUnavailable(executor);
    }

    private void onExecutorWithoutExecution(Executor executor) {
        listener.onExecutorWithoutExecution(executor);
    }

    private JsonNode toJson(String body) throws MonitorException {
        try {
            return mapper.readTree(body);
        } catch (IOException ex) {
            throw new MonitorException("Can't read overview JSON.", ex);
        }
    }

    private Execution getExecution(Executor executor, JsonNode overview)
            throws MonitorException {
        Execution execution = executions.getExecution(executor);
        if (execution == null) {
            return executions.getExecution(overview);
        }
        return execution;
    }

    private void reportExecutorHasExecution(
            Executor executor, Execution execution) {
        listener.onExecutorHasExecution(execution, executor);
    }

    private void updateFromOverview(
            Execution execution, JsonNode overview) {
        listener.onOverview(execution, overview);
    }

}
