package com.linkedpipes.etl.executor.monitor.events;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;

public interface EventListener {

    void onExecutionStatusDidChange(
            Execution execution, ExecutionStatus oldStatus);

    void onExecutionHasFinalData(Execution execution);

}
