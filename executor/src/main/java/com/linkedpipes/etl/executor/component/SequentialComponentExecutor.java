package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.logging.ExecutionLogger;
import org.slf4j.MDC;

class SequentialComponentExecutor implements Runnable {


    private final SequentialExecution executable;

    private ExecutionObserver execution;

    private ExecutionComponent component;

    private ExecutorException exception;

    private ExecutionContext context;

    public SequentialComponentExecutor(
            SequentialExecution executable,
            ExecutionObserver execution,
            ExecutionComponent component,
            ExecutionContext context) {
        this.executable = executable;
        this.execution = execution;
        this.component = component;
        this.context = context;
    }

    @Override
    public void run() {
        MDC.put(ExecutionLogger.EXECUTION_MDC, null);
        try {
            execution.onComponentUserCodeBegin(component);
            executable.execute(context);
            execution.onComponentUserCodeSuccessful(component);
        } catch (Throwable ex) {
            execution.onComponentUserCodeFailed(component, ex);
            exception = new ExecutorException(
                    "PipelineComponent execution failed.", ex);
        }
        MDC.remove(ExecutionLogger.EXECUTION_MDC);
    }

    public ExecutorException getException() {
        return exception;
    }
    
}
