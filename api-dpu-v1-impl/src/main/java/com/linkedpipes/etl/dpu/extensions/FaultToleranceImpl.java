package com.linkedpipes.etl.dpu.extensions;

import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.extensions.FaultTolerance;
import com.linkedpipes.etl.executor.api.v1.component.Component.InitializationFailed;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

/**
 *
 * @author Škoda Petr
 */
public class FaultToleranceImpl implements FaultTolerance, ManageableExtension {

    @Override
    public void call(Procedure procedure) throws DataProcessingUnit.ExecutionFailed {
        try {
            procedure.action();
        } catch (Exception ex) {
            throw new DataProcessingUnit.ExecutionFailed("User operation failed.", ex);
        }
    }

    @Override
    public <T> T call(Function<T> function) throws DataProcessingUnit.ExecutionFailed {
        try {
            return function.action();
        } catch (Exception ex) {
            throw new DataProcessingUnit.ExecutionFailed("User operation failed.", ex);
        }
    }

    @Override
    public void initialize(SparqlSelect definition, String componentUri, String graph) throws InitializationFailed {
        // TODO Initialize here ..
    }

    @Override
    public void preExecution() {
        // No operation here.
    }

    @Override
    public void postExecution() {
        // No operation here.
    }

}
