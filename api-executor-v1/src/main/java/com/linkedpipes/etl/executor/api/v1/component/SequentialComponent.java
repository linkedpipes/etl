package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.RdfException;

/**
 * Specialized interface for executable component. Represent
 * a component that is executed in it own thread with given
 * full input and output data.
 */
public interface SequentialComponent extends Component {

    /**
     * Execute component with given content.
     */
    public void execute() throws RdfException;

}
