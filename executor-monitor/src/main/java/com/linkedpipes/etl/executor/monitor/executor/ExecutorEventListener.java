package com.linkedpipes.etl.executor.monitor.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.execution.Execution;

public interface ExecutorEventListener {

    void onAttachExecutor(Execution execution, Executor executor);

    /**
     * Called when it is clear that the execution no longer is executing
     * the execution.
     */
    void onDetachExecutor(Execution execution);

    /**
     * Called when it is not possible to establish connection with executor.
     */
    void onUnresponsiveExecutor(Execution execution);

    void onOverview(Execution execution, JsonNode overview);

}
