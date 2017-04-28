package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import org.slf4j.MDC;

class SequentialComponentExecutor implements Runnable {

    private final SequentialExecution executable;

    private ExecutionObserver execution;

    private ExecutionModel.Component component;

    private ExecutorException exception;

    public SequentialComponentExecutor(
            SequentialExecution executable,
            ExecutionObserver execution,
            ExecutionModel.Component component) {
        this.executable = executable;
        this.execution = execution;
        this.component = component;
    }

    @Override
    public void run() {
        MDC.put(LoggerFacade.EXECUTION_MDC, null);
        try {
            execution.onComponentUserCodeBegin(component);
            executable.execute();
            execution.onComponentUserCodeSuccessful(component);
        } catch (Throwable ex) {
            execution.onComponentUserCodeFailed(component, ex);
            exception = new ExecutorException(
                    "Component execution failed.", ex);
        }
        MDC.remove(LoggerFacade.EXECUTION_MDC);
    }

    public ExecutorException getException() {
        return exception;
    }
    
}
