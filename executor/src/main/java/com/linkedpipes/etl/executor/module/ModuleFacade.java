package com.linkedpipes.etl.executor.module;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.Plugin;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.pipeline.PipelineDefinition;

import java.util.Collection;

/**
 * @author Škoda Petr
 */
public interface ModuleFacade {

    public class ModuleException extends ExecutorException {

        public ModuleException(String messages, Object... args) {
            super(messages, args);
        }
    }

    /**
     * Collection of all loaded execution listeners.
     *
     * @return
     */
    public Collection<Plugin.PipelineListener> getPipelineListeners()
            throws ModuleException;

    /**
     * Create and return component that matches given specification.
     *
     * @param definition
     * @param subject
     * @param context Context given to new component.
     * @return Never null.
     */
    public Component getComponent(PipelineDefinition definition,
            String subject, Component.Context context)
            throws ModuleException;

    /**
     * Create and return manageable data unit that matches given specification.
     *
     * @param definition
     * @param subject
     * @return Does not return null!
     */
    public ManageableDataUnit getDataUnit(PipelineDefinition definition,
            String subject) throws ModuleException;

}
