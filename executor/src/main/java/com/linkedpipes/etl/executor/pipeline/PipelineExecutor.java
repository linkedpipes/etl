package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.PipelineExecutionObserver;
import com.linkedpipes.etl.executor.plugin.v1.ManageableComponent;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP;
import com.linkedpipes.etl.executor.component.ComponentExecutor;
import com.linkedpipes.etl.executor.dataunit.DataUnitInstanceSource;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.ResourceManager;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import com.linkedpipes.etl.executor.plugin.BannedComponent;
import com.linkedpipes.etl.executor.plugin.PluginException;
import com.linkedpipes.etl.executor.pipeline.model.ExecutionType;
import com.linkedpipes.etl.executor.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.executor.plugin.PluginServiceHolder;
import com.linkedpipes.etl.executor.rdf.RdfSourceWrap;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class PipelineExecutor {

    private static final Logger LOG =
            LoggerFactory.getLogger(PipelineExecutor.class);

    private final ResourceManager resources;

    private final LoggerFacade loggerFacade = new LoggerFacade();

    private final PluginServiceHolder moduleFacade;

    private Pipeline pipeline;

    private final ExecutionObserver execution;

    private DataUnitManager dataUnitManager;

    private boolean cancelExecution = false;

    /**
     * Current component executor, we need to access to this
     * objects because of {@link #cancelExecution()}.
     */
    private ComponentExecutor executor = null;

    private final Map<String, ManageableComponent>
            componentsInstances = new HashMap<>();

    /**
     * Create the pipeline executor.
     *
     * @param directory Execution directory.
     * @param iri       ExecutionObserver IRI.
     * @param modules   Module service.
     */
    public PipelineExecutor(
            File directory, String iri, PluginServiceHolder modules) {
        // We assume that the directory we are executing is in the
        // directory with other executions.
        MDC.put(LoggerFacade.EXECUTION_MDC, null);
        this.resources = new ResourceManager(
                directory.getParentFile(), directory);
        this.loggerFacade.prepareAppendersForExecution(
                resources.getExecutionLogFile(),
                "INFO");
        this.moduleFacade = modules;
        this.execution = new ExecutionObserver(resources, iri);
        this.execution.onExecutionBegin();
        MDC.remove(LoggerFacade.EXECUTION_MDC);
    }

    public void execute() {
        LOG.info("PipelineExecutor.execute ... ");
        MDC.put(LoggerFacade.EXECUTION_MDC, null);
        try {
            if (initialize()) {
                execution.onComponentsExecutionBegin();
                executeComponents();
                execution.onComponentsExecutionEnd();
            }
        } catch (Throwable t) {
            execution.onExecutionFailedOnThrowable(t);
        } finally {
            try {
                terminate();
            } catch (Throwable t) {
                LOG.error("Can't cleanup after pipeline execution!", t);
            }
        }
        MDC.remove(LoggerFacade.EXECUTION_MDC);
        LOG.info("PipelineExecutor.execute ... done ");
    }

    public void cancelExecution() {
        synchronized (this) {
            if (cancelExecution) {
                return;
            }
            MDC.put(LoggerFacade.EXECUTION_MDC, null);
            LOG.info("ExecutionObserver cancelled!");
            cancelExecution = true;
            // Notify the executor if it's not null.
            final ComponentExecutor currentExecutor = executor;
            if (currentExecutor != null) {
                LOG.info("Cancelling component!");
                currentExecutor.cancel();
            }
            execution.onCancelRequest();
            MDC.remove(LoggerFacade.EXECUTION_MDC);
        }
    }

    public ExecutionObserver getExecution() {
        return execution;
    }

    private boolean initialize() {
        try {
            loadPipeline();
        } catch (ExecutorException ex) {
            execution.onCantLoadPipeline(ex);
            return false;
        }
        updateLogging();
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
            initializeDataUnits();
        } catch (ExecutorException ex) {
            execution.onDataUnitsLoadingFailed(ex);
            return false;
        }
        try {
            loadComponents();
        } catch (ExecutorException ex) {
            execution.onComponentsLoadingFailed(ex);
            return false;
        } catch (Throwable ex) {
            execution.onComponentsLoadingFailed(
                    new LpException("Initialization failed on throwable.", ex));
            return false;
        }
        return true;
    }

    private void loadPipeline() throws ExecutorException {
        File definitionFile = locatePipelineDefinitionFile();
        pipeline = new Pipeline();
        File workingDirectory =
                resources.getWorkingDirectory("pipeline_repository");
        pipeline.load(definitionFile, workingDirectory);
        execution.onPipelineLoaded(pipeline.getModel());
    }

    /**
     * We start with default INFO log level, we need to update it.
     */
    private void updateLogging() {
        String level = pipeline.getModel().getLogLevel();
        LOG.info("Changing log level to: {}", level);
        loggerFacade.destroyExecutionAppenders();
        loggerFacade.prepareAppendersForExecution(
                resources.getExecutionLogFile(), level);
    }

    private File locatePipelineDefinitionFile() throws ExecutorException {
        File definitionFile = resources.getDefinitionFile();
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
        // Load data from previous executions.
        for (PipelineComponent component :
                pipeline.getModel().getComponents()) {
            if (!component.isPlannedForExecution()) {
                continue;
            }
            if (component.getExecution() == null) {
                continue;
            }
            resolveWorkingDirectory(component);
        }
    }

    /**
     * Load component's original execution and set last working directory,
     * reading its value from the original execution.
     */
    private void resolveWorkingDirectory(
            PipelineComponent component)
            throws ExecutorException {
        String execution = component.getExecution();
        File pipelineFile = resources.resolveExecutionPath(
                execution, "pipeline.trig");
        try (InputStream stream = new FileInputStream(pipelineFile)) {
            RDFParser parser = Rio.createParser(RDFFormat.TRIG);
            parser.setRDFHandler(new AbstractRDFHandler() {

                @Override
                public void handleStatement(Statement st) {
                    if (!st.getSubject().stringValue().equals(
                            component.getIri())) {
                        return;
                    }
                    if (!st.getPredicate().stringValue().equals(
                            LP.HAS_WORKING_DIRECTORY)) {
                        return;
                    }
                    component.setLastWorkingDirectory(
                            new File(URI.create(st.getObject().stringValue())));
                }
            });
            parser.parse(stream, "http://localhost/default");
        } catch (Exception ex) {
            throw new ExecutorException(
                    "Can't resolve working directory for: %s from %s",
                    component, execution, ex);
        }
    }

    private void notifyObserversOnBeginning() throws ExecutorException {
        try {
            for (PipelineExecutionObserver observer :
                    moduleFacade.getPipelineListeners()) {
                observer.onPipelineBegin(pipeline.getPipelineIri(),
                        new RdfSourceWrap(pipeline.getSource(),
                                pipeline.getPipelineGraph()));
            }
        } catch (LpException ex) {
            throw new ExecutorException("Observer error.", ex);
        }
    }

    private void initializeDataUnits() throws ExecutorException {
        dataUnitManager = new DataUnitManager(pipeline.getModel());
        DataUnitInstanceSource dataUnitInstanceSource =
                (iri) -> moduleFacade.getDataUnit(pipeline, iri);
        dataUnitManager.initialize(dataUnitInstanceSource,
                execution.getModel().getDataUnitsForInitialization());
    }

    private void loadComponents() throws ExecutorException {
        boolean loadingFailed = false;
        for (PipelineComponent component :
                pipeline.getModel().getComponents()) {
            if (!shouldLoadInstanceForComponent(component)) {
                continue;
            }
            String iri = component.getIri();
            ManageableComponent instance;
            try {
                instance = moduleFacade.getComponent(pipeline, iri);
                componentsInstances.put(component.getIri(), instance);
            } catch (BannedComponent ex) {
                execution.onCantLoadComponentJar(
                        component, new LpException(
                                "This component is banned on this instance."));
                LOG.error("Banned component.", ex);
                loadingFailed = true;
            } catch (PluginException ex) {
                execution.onCantLoadComponentJar(component, ex);
                LOG.error("Can't load component.", ex);
                loadingFailed = true;
            }
        }
        if (loadingFailed) {
            throw new ExecutorException("Can't load components.");
        }
    }

    private boolean shouldLoadInstanceForComponent(
            PipelineComponent component) {
        return component.getExecutionType() == ExecutionType.EXECUTE;
    }

    private void executeComponents() {
        for (PipelineComponent pplComponent
                : pipeline.getModel().getComponents()) {
            if (!executeComponent(pplComponent)) {
                break;
            }
            synchronized (this) {
                if (cancelExecution) {
                    break;
                }
            }
        }
    }

    /**
     * Return false if execution failed.
     */
    private boolean executeComponent(PipelineComponent pplComponent) {
        ExecutionComponent execComponent =
                this.execution.getModel().getComponent(pplComponent);

        this.execution.onBeforeComponentExecution(execComponent);

        try {
            this.executor = getExecutor(pplComponent);
        } catch (ExecutorException ex) {
            this.execution.onCantCreateComponentExecutor(execComponent, ex);
            return this.afterComponentExecution(execComponent);
        }

        boolean shouldContinue = this.executor.execute(this.dataUnitManager);
        boolean messagesSaved = this.afterComponentExecution(execComponent);
        this.executor = null;
        return shouldContinue && messagesSaved;
    }

    private boolean afterComponentExecution(ExecutionComponent component) {
        try {
            this.execution.onAfterComponentExecution(component);
            return true;
        } catch (IOException ex) {
            this.execution.onCantSaveComponentMessages(component, ex);
            return false;
        }
    }

    private ComponentExecutor getExecutor(PipelineComponent component)
            throws ExecutorException {
        ManageableComponent instance =
                componentsInstances.get(component.getIri());
        return ComponentExecutor.create(
                pipeline, execution, component, instance);
    }

    private void terminate() {
        notifyObserversOnEnding();
        try {
            pipeline.save(resources.getPipelineFile());
        } catch (ExecutorException ex) {
            LOG.info("Can't save pipeline.", ex);
        }
        if (dataUnitManager != null) {
            dataUnitManager.close();
        }
        pipeline.closeRepository();
        deleteDebugData();
        execution.onExecutionEnd();
        loggerFacade.destroyExecutionAppenders();
        deleteLogFiles();
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

    private void deleteDebugData() {
        if (pipeline.getModel().isDeleteWorkingData()) {
            try {
                FileUtils.deleteDirectory(resources.getRootWorkingDirectory());
            } catch (IOException ex) {
                LOG.error("Can't delete working directory.", ex);
            }
        }
    }

    private void deleteLogFiles() {
        if (pipeline.getModel().isDeleteLogDataOnSuccess()
                && execution.isExecutionSuccessful()) {
            try {
                FileUtils.deleteDirectory(resources.getExecutionLogDirectory());
            } catch (IOException ex) {
                LOG.error("Can't delete log directory.", ex);
            }
        }
    }

}

