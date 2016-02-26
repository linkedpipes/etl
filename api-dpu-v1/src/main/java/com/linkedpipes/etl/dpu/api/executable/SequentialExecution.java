package com.linkedpipes.etl.dpu.api.executable;

import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;

/**
 * Interface of component designed for sequential execution.
 *
 * @author Petr Škoda
 */
public interface SequentialExecution extends DataProcessingUnit {

    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException;

}
