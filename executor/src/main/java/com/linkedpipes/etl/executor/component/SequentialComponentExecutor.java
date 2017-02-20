package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import org.slf4j.MDC;

class SequentialComponentExecutor implements Runnable {

    private final SequentialExecution executable;

    private ExecutorException exception;

    public SequentialComponentExecutor(SequentialExecution executable) {
        this.executable = executable;
    }

    @Override
    public void run() {
        MDC.put(LoggerFacade.EXECUTION_MDC, null);
        try {
            executable.execute();
        } catch (Throwable ex) {
            exception = new ExecutorException(
                    "Component execution failed.", ex);
        }
        MDC.remove(LoggerFacade.EXECUTION_MDC);
    }

    public ExecutorException getException() {
        return exception;
    }
    
}
