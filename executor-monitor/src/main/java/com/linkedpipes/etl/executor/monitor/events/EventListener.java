package com.linkedpipes.etl.executor.monitor.events;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;

public interface EventListener {

    default void onExecutionStatusDidChange(
            Execution execution, ExecutionStatus oldStatus)  {
        // Do nothing;
    }

    default void onExecutionHasFinalData(Execution execution)  {
        // Do nothing;
    }

    default void onExecutionFacadeReady(ExecutionFacade executions) {
        // Do nothing;
    }

    default void onTimeHour() {
        // Do nothing;
    }

}
