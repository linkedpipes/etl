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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PipelineExecutor {

    private static final Logger LOG =
            LoggerFactory.getLogger(PipelineExecutor.class);

    private final ResourceManager resources;

    private final LoggerFacade loggerFacade = new LoggerFacade();

    private final ModuleFacade moduleFacade;

    private Pipeline pipeline;

    private final Execution execution;

    private final DataUnitManager dataUnitManager = new DataUnitManager();

    private boolean cancelExecution = false;

    /**
     * Current component executor, we need to access to this
     * objects because of {@link #cancelExecution()}.
     */
    private ComponentExecutor executor = null;

    private final Map<String, ManageableComponent>
            componentsInstances = new HashMap<>();

    /**
     * @param directory
     * @param iri Execution IRI.
     * @param modules
     */
    public PipelineExecutor(File directory, String iri, ModuleFacade modules) {
        // We assume that the directory we are executing is in the
        // directory with other executions.
        MDC.put(LoggerFacade.EXECUTION_MDC, null);
        this.resources = new ResourceManager(
                directory.getParentFile(), directory);
        this.loggerFacade.prepareAppendersForExecution(
                resources.getExecutionDebugLogFile(),
                resources.getExecutionInfoLogFile());
        this.moduleFacade = modules;
        this.execution = new Execution(resources, iri);
        MDC.remove(LoggerFacade.EXECUTION_MDC);
    }

    public void execute() {
        MDC.put(LoggerFacade.EXECUTION_MDC, null);
        execution.onInitializationBegin();
        if (initialize()) {
            execution.onComponentsExecutionBegin();
            executeComponents();
            execution.onComponentsExecutionEnd();
        }
        terminate();
        MDC.remove(LoggerFacade.EXECUTION_MDC);
    }

    public synchronized void cancelExecution() {
        if (cancelExecution) {
            return;
        }
        cancelExecution = true;
        // Notify the executor if it's not null.
        final ComponentExecutor currentExecutor = executor;
        if (currentExecutor != null) {
            currentExecutor.cancel();
        }
        execution.onCancelRequest();
    }

    public Execution getExecution() {
        return execution;
    }

    private boolean initialize() {
        try {
            loadPipeline();
        } catch (ExecutorException ex) {
            execution.onInvalidPipeline(ex);
            return false;
        }
        try {
            preparePipelineDefinition();
        } catch (ExecutorException ex) {
            execution.onCantPreparePipeline(ex);
            return false;
        }
        try {
            notifyObserversOnBeginning();
        } catch (ExecutorException ex) {
            execution.onObserverBeginFailed(ex);
            return false;
        }
        try {
            loadDataUnits();
        } catch (ExecutorException ex) {
            execution.onDataUnitsLoadingFailed(ex);
            return false;
        }
        try {
            loadComponents();
        } catch (ExecutorException ex) {
            execution.onComponentsLoadingFailed(ex);
            return false;
        }
        return true;
    }

    private void loadPipeline() throws ExecutorException {
        final File definitionFile = locatePipelineDefinitionFile();
        this.pipeline = new Pipeline();
        execution.bindToPipeline(pipeline);
        final File workingDirectory =
                resources.getWorkingDirectory("pipeline_repository");
        this.pipeline.load(definitionFile, workingDirectory);
        execution.onPipelineLoaded();
    }

    private File locatePipelineDefinitionFile() throws ExecutorException {
        final File definitionFile = resources.getDefinitionFile();
        if (definitionFile == null) {
            throw new ExecutorException("Missing definition file!");
        }
        return definitionFile;
    }

    /**
     * Resolve requirements and update pipeline for out execution
     * environment.
     */
    private void preparePipelineDefinition() throws ExecutorException {
        try {
            RequirementProcessor.handle(pipeline.getSource(),
                    pipeline.getPipelineGraph(), resources);
        } catch (LpException ex) {
            throw new ExecutorException("Can't update pipeline.", ex);
        }
    }

    private void notifyObserversOnBeginning() throws ExecutorException {
        try {
            for (PipelineExecutionObserver observer :
                    moduleFacade.getPipelineListeners()) {
                observer.onPipelineBegin(pipeline.getPipelineIri(),
                        pipeline.getPipelineGraph(), pipeline.getSource());
            }
        } catch (LpException ex) {
            throw new ExecutorException("Observer error.", ex);
        }
    }

    private void loadDataUnits() throws ExecutorException {
        final DataUnitManager.DataUnitInstanceSource dataUnitInstanceSource =
                (iri) -> {
                    return moduleFacade.getDataUnit(pipeline, iri);
                };
        dataUnitManager.initialize(dataUnitInstanceSource,
                execution.getUsedDataUnits());
    }

    private void loadComponents() throws ExecutorException {
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
                throw new ExecutorException(
                        "Can't bindToPipeline component: {}",
                        component.getLabel(), ex);
            }
            componentsInstances.put(component.getIri(), instance);
        }
    }

    private void executeComponents() {
        for (PipelineModel.Component pplComponent
                : pipeline.getModel().getComponents()) {
            if (!executeComponent(pplComponent)) {
                break;
            }
            if (cancelExecution) {
                break;
            }
        }
    }

    /**
     * @param pplComponent
     * @return False if execution failed.
     */
    private boolean executeComponent(PipelineModel.Component pplComponent) {
        try {
            executor = getExecutor(pplComponent);
        } catch (ExecutorException ex) {
            executor = null;
            final Execution.Component execComponent =
                    execution.getComponent(pplComponent);
            execution.onInvalidComponent(execComponent, ex);
            return false;
        }
        final boolean canContinue = executor.execute(dataUnitManager);
        executor = null;
        if (!canContinue) {
            return false;
        }
        return true;
    }

    private ComponentExecutor getExecutor(PipelineModel.Component component)
            throws ExecutorException {
        final ManageableComponent instance =
                componentsInstances.get(component.getIri());
        return ComponentExecutor.create(pipeline,
                execution, component, instance);
    }

    private void terminate() {
        notifyObserversOnEnding();
        try {
            pipeline.save(resources.getPipelineFile());
        } catch (ExecutorException ex) {
            LOG.info("Can't save pipeline.", ex);
        }
        dataUnitManager.close();
        pipeline.close();
        execution.onExecutionEnd();
        execution.close();
        loggerFacade.destroyExecutionAppenders();
    }

    private void notifyObserversOnEnding() {
        try {
            for (PipelineExecutionObserver observer :
                    moduleFacade.getPipelineListeners()) {
                observer.onPipelineEnd();
            }
        } catch (ExecutorException ex) {
            execution.onObserverEndFailed(ex);
        }
    }


}

