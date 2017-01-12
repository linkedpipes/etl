package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.api.v1.component.ManageableComponent;
import com.linkedpipes.etl.executor.component.ComponentExecutor;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.Execution;
import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import com.linkedpipes.etl.executor.module.ModuleException;
import com.linkedpipes.etl.executor.module.ModuleFacade;
import org.slf4j.MDC;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Execute pipeline in given directory.
 */
public class PipelineExecutor {

    private final ResourceManager resources;

    private final LoggerFacade loggerFacade = new LoggerFacade();

    private final ModuleFacade moduleFacade;

    private Pipeline pipeline;

    private Execution execution;

    private DataUnitManager dataUnitManager;

    private boolean cancel = false;

    /**
     * Current executor, we need to access to this objects because of
     * cancel.
     */
    private ComponentExecutor executor = null;

    /**
     * @param directory
     * @param modules
     */
    public PipelineExecutor(File directory, ModuleFacade modules) {
        // We assume that the directory we are executing is in the
        // directory with other executions.
        this.resources =
                new ResourceManager(directory.getParentFile(), directory);
        this.loggerFacade.setSystemAppender(resources.getExecutionLogFile());
        this.moduleFacade = modules;
    }

    /**
     * Execute pipeline.
     */
    public void execute() {
        MDC.put(LoggerFacade.SYSTEM_MDC, null);
        // Initialize.
        try {
            initialize();
        } catch (ExecutorException ex) {
            if (execution == null) {
                // TODO Report pipeline loading failure in a generic way.
                throw new UnsupportedOperationException(ex);
            } else {
                execution.onInitializationFailed(ex);
            }
            terminate();
        }
        try {
            onPipelineBegin();
        } catch (ExecutorException ex) {
            execution.onObserverBeginFailed(ex);
            terminate();
        }
        // Load components.
        final Map<String, ManageableComponent> components;
        try {
            components = loadComponents();
        } catch (ExecutorException ex) {
            execution.onComponentsLoadingFailed(ex);
            terminate();
            return;
        }
        // Execute components.
        for (PipelineModel.Component pplComponent
                : pipeline.getModel().getComponents()) {
            final Execution.Component execComponent =
                    execution.getComponent(pplComponent);
            final ManageableComponent instance =
                    components.get(pplComponent.getIri());
            // Get component executor.
            try {
                executor = ComponentExecutor.create(pipeline,
                        execution, pplComponent, instance);
            } catch (ExecutorException ex) {
                executor = null;
                execution.onInvalidComponent(execComponent, ex);
                break;
            }
            // Now we know the executor is not null. But the execution
            // might have been cancelled before the executor was assigned.
            if (cancel) {
                break;
            }
            // Execute component, we also have to properly work
            // with the MDC.
            try {
                execution.onComponentInitialize(execComponent);
                MDC.remove(LoggerFacade.SYSTEM_MDC);
                executor.initialize(dataUnitManager);
                MDC.put(LoggerFacade.SYSTEM_MDC, null);
                execution.onComponentBegin(execComponent);
                MDC.remove(LoggerFacade.SYSTEM_MDC);
                executor.execute();
                MDC.put(LoggerFacade.SYSTEM_MDC, null);
            } catch (ExecutorException ex) {
                execution.onComponentFailed(execComponent, ex);
                break;
            } finally {
                executor = null;
            }
            execution.onComponentEnd(execComponent);
            if (cancel) {
                break;
            }
        }
        terminate();
    }

    /**
     * Cause pipeline to stop as soon as possible.
     */
    public void cancel() {
        if (cancel) {
            return;
        }
        // First set cancel flag.
        cancel = true;
        // Notify the executor if it's not null.
        final ComponentExecutor currentExecutor = executor;
        if (currentExecutor != null) {
            currentExecutor.cancel();
        }
        execution.onCancel();
    }

    protected void initialize() throws ExecutorException {
        final File definitionFile = resources.getDefinitionFile();
        if (definitionFile == null) {
            throw new ExecutorException("Missing definition file!");
        }
        this.pipeline = new Pipeline();
        this.pipeline.load(definitionFile,
                resources.getWorkingDirectory("pipeline_repository"));
        execution = new Execution(pipeline, resources);
        execution.onExecutionBegin();
        dataUnitManager = new DataUnitManager(
                pipeline, execution, moduleFacade);
    }

    /**
     * @return Instantiated component used in a pipeline.
     */
    protected Map<String, ManageableComponent> loadComponents()
            throws ExecutorException {
        final Map<String, ManageableComponent> instances = new HashMap<>();
        for (PipelineModel.Component component :
                pipeline.getModel().getComponents()) {
            if (!component.isLoadInstance()) {
                continue;
            }
            final ManageableComponent instance;
            try {
                instance = moduleFacade.getComponent(pipeline,
                        component.getIri());
            } catch (ModuleException ex) {
                throw new ExecutorException("Can't initialize component: {}",
                        component.getLabel(), ex);
            }
            instances.put(component.getIri(), instance);
        }
        return instances;
    }

    /**
     * Must be called after the execution.
     */
    protected void terminate() {
        try {
            onPipelineEnd();
        } catch (ExecutorException ex) {
            execution.onObserverEndFailed(ex);
        }
        dataUnitManager.close();
        pipeline.close();
        execution.onExecutionEnd();
        execution.close();
        loggerFacade.destroyAll();
        MDC.remove(LoggerFacade.SYSTEM_MDC);
    }

    /**
     * Notify observers that the pipeline execution is about to begin.
     */
    protected void onPipelineBegin() throws ExecutorException {
        for (PipelineExecutionObserver observer :
                moduleFacade.getPipelineListeners()) {
            try {
                observer.onPipelineBegin(pipeline.getPipelineIri(),
                        pipeline.getPipelineGraph(), pipeline.getSource());
            } catch (LpException ex) {
                throw new ExecutorException("Observer error.", ex);
            }
        }
    }

    /**
     * Notify observers that the pipeline execution has finished.
     */
    protected void onPipelineEnd() throws ExecutorException {
        for (PipelineExecutionObserver observer :
                moduleFacade.getPipelineListeners()) {
            observer.onPipelineEnd();
        }
    }

}

