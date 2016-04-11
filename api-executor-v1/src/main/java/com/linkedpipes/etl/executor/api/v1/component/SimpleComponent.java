package com.linkedpipes.etl.executor.api.v1.component;

/**
 * Specialized interface for a base executable component.
 *
 * @author Škoda Petr
 */
public interface SimpleComponent extends BaseComponent {

    /**
     * Execute component with given content.
     *
     * @throws BaseComponent.ComponentFailed
     */
    public void execute() throws ComponentFailed;

}
