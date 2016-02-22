package com.linkedpipes.etl.dpu.test;

import com.linkedpipes.etl.dpu.api.DataProcessingUnit.ExecutionFailed;
import com.linkedpipes.etl.dpu.api.extensions.FaultTolerance;

/**
 *
 * @author Petr Škoda
 */
final class MockedFaultTolerance implements FaultTolerance {

    @Override
    public void call(Procedure procedure) throws ExecutionFailed {
        try {
            procedure.action();
        } catch (Exception ex) {
            throw new ExecutionFailed("Action failed.", ex);
        }
    }

    @Override
    public <T> T call(Function<T> function) throws ExecutionFailed {
        try {
            return function.action();
        } catch (Exception ex) {
            throw new ExecutionFailed("Action failed.", ex);
        }
    }

}
