package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.RdfException;

/**
 * Specialized interface for executable component.
 *
 * @author Škoda Petr
 */
public interface SequentialComponent extends BaseComponent {

    /**
     * Execute component with given content.
     *
     * @throws com.linkedpipes.etl.executor.api.v1.RdfException
     */
    public void execute() throws RdfException;

}
