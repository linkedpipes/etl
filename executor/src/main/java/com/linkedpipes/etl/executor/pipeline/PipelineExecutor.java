package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.context.Context;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.plugin.ExecutionListener;
import com.linkedpipes.etl.executor.component.ComponentExecutor;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager.CantInitializeDataUnit;
import com.linkedpipes.etl.executor.event.EventFactory;
import com.linkedpipes.etl.executor.event.EventManager;
import com.linkedpipes.etl.executor.event.ExecutionFailed;
import com.linkedpipes.etl.executor.execution.ExecutionModel;
import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import com.linkedpipes.etl.executor.module.ModuleFacade;
import com.linkedpipes.etl.executor.module.ModuleFacade.ModuleException;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.MDC;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class PipelineExecutor implements EventManager.EventListener {

    private static class InitializationFailure extends Exception {

        InitializationFailure(Throwable cause) {
            super(cause);
        }

    }

    private static final Logger LOG
            = LoggerFactory.getLogger(PipelineExecutor.class);

    private final ResourceManager resources;

    private final PipelineDefinition pipeline;

    private final LoggerFacade loggerFacade = new LoggerFacade();

    private ExecutionModel execution;

    private EventManager events;

    private final ModuleFacade modules;

    private DataUnitManager dataUnits;

    /**
     * Current component executor.
     */
    private ComponentExecutor componentExecutor = null;

    /**
     * If True then the execution should end as soon as possible.
     */
    private boolean stopExecution = false;

    public PipelineExecutor(File executionDirectory, ModuleFacade modules) {
        this.resources = new ResourceManager(executionDirectory);
        this.pipeline = new PipelineDefinition(
                this.resources.getWorkingDirectory("definition"));
        this.loggerFacade.setSystemAppender(resources.getExecutionLogFile());
        this.modules = modules;
    }

    /**
     *
     * @param iri Execution IRI.
     * @return False if initialization failed and pipeline can not be executed.
     */
    public boolean initialize(String iri) {
        MDC.put(LoggerFacade.SYSTEM_MDC, null);
        events = new EventManager(iri);
        events.addListener(this);
        // Load definition.
        try {
            pipeline.initialize(resources);
        } catch (PipelineDefinition.InitializationFailed ex) {
            events.publish(EventFactory.initializationFailed(
                    "Can't load pipeline definition.", ex));
            afterExecution();
            MDC.remove(LoggerFacade.SYSTEM_MDC);
            return false;
        }
        // Create modules.
        execution = new ExecutionModel(pipeline.getPipelineModel(), iri,
                resources);
        events.addListener(execution);
        dataUnits = new DataUnitManager(pipeline, execution,
                modules);
        //
        MDC.remove(LoggerFacade.SYSTEM_MDC);
        return true;
    }

    /**
     * Stop execution.
     */
    public void stop() {
        final ComponentExecutor executor = componentExecutor;
        if (executor != null) {
            executor.cancel();
        }
        events.publish(EventFactory.executionCancelled());
        stopExecution = true;
    }

    /**
     * Write status of currently executed pipeline.
     *
     * @param stream
     * @param format
     */
    public void writeStatus(OutputStream stream, RDFFormat format) {
        execution.write(stream, format);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ExecutionFailed) {
            stopExecution = true;
        }
    }

    public void execute() {
        beforeExecution();
        // Get all components, so if some is missing we find out at the
        // beginning of a pipeline.
        final Map<String, Component> componenInstances;
        try {
            sendExecutionBeginNotification();
            componenInstances = initializeComponents();
            dataUnits.prepareDataUnits();
        } catch (InitializationFailure | CantInitializeDataUnit ex) {
            events.publish(EventFactory.initializationFailed(
                    "Initialization failed.", ex));
            afterExecution();
            return;
        }
        // Periodic save.
        execution.save();
        //
        for (ExecutionModel.Component component : execution.getComponents()) {
            final ComponentExecutor executor = ComponentExecutor.create(
                    modules, dataUnits, events, pipeline,
                    execution, component.getIri(),
                    componenInstances.get(component.getIri()));
            if (executor == null) {
                events.publish(EventFactory.executionFailed(
                        "Can't get executor."));
            } else {
                componentExecutor = executor;
                executor.execute();
                componentExecutor = null;
            }
            if (stopExecution) {
                break;
            }
            // Periodic save.
            execution.save();
        }
        afterExecution();
    }

    private void beforeExecution() {
        MDC.put(LoggerFacade.SYSTEM_MDC, null);
        events.publish(EventFactory.executionBegin());
    }

    /**
     * Called after the execution, save all data and close all resources.
     */
    private void afterExecution() {
        // Close data units.
        dataUnits.close(events);
        // Notify plugins that we are done and they can close too.
        // Behind this point we can't work with data units.
        sendExecutionEndNotification();
        // Close pipeline definition.
        pipeline.close();
        // Execution must be saved at the end - from this point
        // the execution is considered to be finished and
        // there must be no furher changes.
        events.publish(EventFactory.executionFinished());
        execution.save();
        //
        loggerFacade.destroyAll();
        MDC.remove(LoggerFacade.SYSTEM_MDC);
    }

    /**
     * Notify all plugins about the start of a new execution.
     *
     * @throws InitializationFailure
     */
    private void sendExecutionBeginNotification() throws InitializationFailure {
        final Context context = (Event message) -> {
            events.publish(message);
        };

        final Collection<ExecutionListener> listeners;
        try {
            listeners = modules.getExecutionListeners();
        } catch (ModuleException ex) {
            throw new InitializationFailure(ex);
        }
        for (ExecutionListener plugin : listeners) {
            try {
                plugin.onExecutionBegin(pipeline,
                        pipeline.getPipelineModel().getIri(),
                        pipeline.getDefinitionGraph(),
                        context);
            } catch (Throwable t) {
                throw new InitializationFailure(t);
            }
        }

    }

    /**
     * Notify all plugins about the end of a new execution.
     */
    private void sendExecutionEndNotification() {
        final Collection<ExecutionListener> listeners;
        try {
            listeners = modules.getExecutionListeners();
        } catch (ModuleException ex) {
            events.publish(EventFactory.executionFailed(
                    "Can't get plugins. Please report this error.", ex));
            return;
        }
        for (ExecutionListener plugin : listeners) {
            try {
                plugin.onExecutionEnd();
            } catch (Throwable t) {
                LOG.error("Call of onExecutionEnd failed.", t);
            }
        }

    }

    /**
     * Create and return instances of used components.
     *
     * TODO We may move this into the ModuleFacade.
     *
     * @return
     */
    private Map<String, Component> initializeComponents()
            throws InitializationFailure {
        final Map<String, Component> result = new HashMap<>();
        for (PipelineModel.Component component
                : pipeline.getPipelineModel().getComponents()) {
            try {
                final Component instance = modules.getComponent(pipeline,
                        component.getIri());
                result.put(component.getIri(), instance);
            } catch (ModuleException ex) {
                throw new InitializationFailure(ex);
            }
        }
        return result;
    }

}
