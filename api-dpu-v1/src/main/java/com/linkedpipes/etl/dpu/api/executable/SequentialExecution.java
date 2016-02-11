/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.linkedpipes.etl.dpu.api.executable;

import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;

/**
 * Interface for sequential execution.
 *
 * @author Petr Škoda
 */
public interface SequentialExecution extends DataProcessingUnit {

    public void execute(DataProcessingUnit.Context context) throws NonRecoverableException;

}
