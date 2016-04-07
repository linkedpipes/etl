package com.linkedpipes.etl.executor.module;

import java.util.Collection;

import com.linkedpipes.etl.executor.api.v1.plugin.ExecutionListener;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.plugin.MessageListener;
import com.linkedpipes.etl.executor.pipeline.PipelineDefinition;

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
    public Collection<ExecutionListener> getExecutionListeners()
            throws ModuleException;

    /**
     * Collection of all loaded message listeners.
     *
     * @return
     * @throws ModuleException
     */
    public Collection<MessageListener> getMessageListeners()
            throws ModuleException;

    /**
     * Create and return component that matches given specification.
     *
     * @param definition
     * @param subject
     * @return Does not return null!
     * @throws ModuleException
     */
    public Component getComponent(PipelineDefinition definition, String subject)
            throws ModuleException;

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
