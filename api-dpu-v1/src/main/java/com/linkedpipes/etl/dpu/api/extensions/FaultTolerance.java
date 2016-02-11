package com.linkedpipes.etl.dpu.api.extensions;

import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;

/**
 *
 * @author Škoda Petr
 */
public interface FaultTolerance {

    @FunctionalInterface
    public interface Procedure {

        public void action() throws NonRecoverableException, Exception;

    }

    @FunctionalInterface
    public interface Function<T> {

        public T action() throws NonRecoverableException, Exception;

    }

    public void call(Procedure procedure) throws DataProcessingUnit.ExecutionFailed;

    public <T> T call(Function<T> function) throws DataProcessingUnit.ExecutionFailed;

}
