package com.linkedpipes.etl.dpu.api.executable;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.etl.dpu.api.Component;

/**
 * Interface of component designed for sequential execution.
 *
 * @author Petr Škoda
 */
public interface SimpleExecution extends Component {

    public void execute(Component.Context context)
            throws NonRecoverableException;

}
