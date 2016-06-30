package com.linkedpipes.etl.executor.module;

import com.linkedpipes.etl.executor.api.v1.Plugin;
import java.util.Collection;

import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.pipeline.PipelineDefinition;
import com.linkedpipes.etl.executor.api.v1.component.BaseComponent;

/**
 *
 * @author Škoda Petr
 */
public interface ModuleFacade {

    public class ModuleException extends Exception {

        public ModuleException(String message) {
            super(message);
        }

        public ModuleException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Collection of all loaded execution listeners.
     *
     * @return
     * @throws ModuleException
     */
    public Collection<Plugin.ExecutionListener> getExecutionListeners()
            throws ModuleException;

    /**
     * Collection of all loaded message listeners.
     *
     * @return
     * @throws ModuleException
     */
    public Collection<Plugin.MessageListener> getMessageListeners()
            throws ModuleException;

    /**
     * Create and return component that matches given specification.
     *
     * @param definition
     * @param subject
     * @param context Context given to new component.
     * @return Never null.
     * @throws ModuleException
     */
    public BaseComponent getComponent(PipelineDefinition definition,
            String subject, Plugin.Context context) throws ModuleException;

    /**
     * Create and return manageable data unit that matches given specification.
     *
     * @param definition
     * @param subject
     * @return Does not return null!
     * @throws ModuleException
     */
    public ManagableDataUnit getDataUnit(PipelineDefinition definition,
            String subject) throws ModuleException;

}
