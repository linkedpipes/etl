package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.plugin.v1.PluginV1Instance;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.RuntimeConfiguration;
import com.linkedpipes.etl.executor.component.configuration.Configuration;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.executor.rdf.RdfSourceWrap;
import com.linkedpipes.etl.executor.rdf.TripleWriterWrap;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfSource;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The component is initialized with data units and configuration in
 * the initialization phase.
 *
 * <p>In the execution phase the execution interface is detected and
 * component is executed.
 */
class ExecuteComponent implements ComponentExecutor {

    private static final String RUNTIME_CONFIGURATION_GRAPH
            = "http://localhost/runtimeConfiguration";

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecuteComponent.class);

    private final Pipeline pipeline;

    private final PipelineComponent pplComponent;

    private final ExecutionComponent execComponent;

    private final PluginV1Instance instance;

    private final ExecutionContext context;

    private final ExecutionObserver execution;

    public ExecuteComponent(
            Pipeline pipeline,
            ExecutionObserver execution,
            PipelineComponent component,
            ExecutionComponent execComponent,
            PluginV1Instance instance) {
        this.pipeline = pipeline;
        this.pplComponent = component;
        this.execComponent = execComponent;
        this.instance = instance;
        this.execution = execution;
        //
        context = new ExecutionContext(execComponent, execution);
    }

    @Override
    public boolean execute(DataUnitManager dataUnitManager) {
        try {
            execution.onExecuteComponentInitializing(execComponent);
            Map<String, DataUnit> dataUnits =
                    dataUnitManager.onComponentWillExecute(execComponent);
            initialize(dataUnits);
            executeInstance();
            execution.onExecuteComponentSuccessful(
                    execComponent, context.isCancelled());
        } catch (ExecutorException ex) {
            try {
                dataUnitManager.onComponentDidExecute(execComponent);
            } catch (ExecutorException e) {
                LOG.error("Can't save data unit after component failed.", e);
            }
            execution.onExecuteComponentFailed(execComponent, ex);
            return false;
        }
        try {
            dataUnitManager.onComponentDidExecute(execComponent);
        } catch (ExecutorException ex) {
            LOG.error("Can't save data unit.", ex);
            execution.onExecuteComponentCantSaveDataUnit(execComponent, ex);
            return false;
        }
        return true;
    }

    @Override
    public void cancel() {
        context.cancel();
    }

    public void initialize(Map<String, DataUnit> dataUnits)
            throws ExecutorException {
        if (instance == null) {
            throw new ExecutorException("The component instance is null: {}",
                    pplComponent.getIri());
        }
        try {
            instance.initialize(dataUnits, context);
        } catch (LpException ex) {
            throw new ExecutorException("Can't bindToPipeline component.", ex);
        }
        configureComponent();
    }

    private void executeInstance() throws ExecutorException {
        executeSequential(instance);
    }

    private void executeSequential(SequentialExecution executable)
            throws ExecutorException {
        SequentialComponentExecutor executor =
                new SequentialComponentExecutor(
                        executable, execution, execComponent, context);
        Thread thread = new Thread(executor, pplComponent.getLabel());
        thread.start();
        waitForThreadToFinish(thread);
        if (executor.getException() != null) {
            throw executor.getException();
        }
    }

    private void waitForThreadToFinish(Thread thread) {
        while (thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                LOG.debug("Ignored interrupt.", ex);
            }
        }
    }

    /**
     * Prepare configuration for this component and load the configuration
     * into the component.
     */
    private void configureComponent() throws ExecutorException {
        RuntimeConfiguration runtimeConfig;
        try {
            runtimeConfig = instance.getRuntimeConfiguration();
        } catch (LpException ex) {
            throw new ExecutorException("Can't get runtime configuration.", ex);
        }

        String resultGraph = pplComponent.getIri() + "/configuration/effective";
        BackendTripleWriter writer = pipeline.configurationWriter(
                pplComponent, resultGraph);

        if (runtimeConfig == null) {
            Configuration.prepareConfiguration(
                    resultGraph, pplComponent,
                    null,
                    null,
                    writer, pipeline);
        } else {
            Configuration.prepareConfiguration(
                    resultGraph, pplComponent,
                    wrapRuntimeConfiguration(runtimeConfig),
                    RUNTIME_CONFIGURATION_GRAPH,
                    writer, pipeline);
        }

        try {
            instance.loadConfiguration(new RdfSourceWrap(
                    pipeline.getSource(),
                    resultGraph));
        } catch (LpException ex) {
            throw new ExecutorException(
                    "Can't load component configuration", ex);
        }
    }

    private BackendRdfSource wrapRuntimeConfiguration(
            RuntimeConfiguration runtimeConfiguration)
            throws ExecutorException {
        BackendRdfSource source = Rdf4jSource.createInMemory();
        BackendTripleWriter writer =
                source.getTripleWriter(RUNTIME_CONFIGURATION_GRAPH);
        try {
            runtimeConfiguration.write(new TripleWriterWrap(writer));
            writer.flush();
        } catch (LpException | RdfUtilsException ex) {
            throw new ExecutorException(
                    "Can't copy runtime configuration.", ex);
        }
        return source;
    }


}
