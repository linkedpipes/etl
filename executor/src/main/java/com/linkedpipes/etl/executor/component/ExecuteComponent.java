package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.ManageableComponent;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.RuntimeConfiguration;
import com.linkedpipes.etl.executor.component.configuration.Configuration;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.ExecutionObserver;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.model.Component;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The component is initialized with data units and configuration in
 * the initialization phase.
 *
 * In the execution phase the execution interface is detected and
 * component is executed.
 */
class ExecuteComponent implements ComponentExecutor {

    private static final String RUNTIME_CONFIGURATION_GRAPH
            = "http://localhost/runtimeConfiguration";

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecuteComponent.class);

    private final Pipeline pipeline;

    private final Component pplComponent;

    private final ExecutionModel.Component execComponent;

    private final ManageableComponent instance;

    private final ExecutionContext context;

    private final ExecutionObserver execution;

    public ExecuteComponent(
            Pipeline pipeline,
            ExecutionObserver execution,
            Component component,
            ExecutionModel.Component execComponent,
            ManageableComponent instance) {
        this.pipeline = pipeline;
        this.pplComponent = component;
        this.execComponent = execComponent;
        this.instance = instance;
        this.execution = execution;
        //
        context = new ExecutionContext(execution, execComponent);
    }

    @Override
    public boolean execute(DataUnitManager dataUnitManager) {
        try {
            execution.onExecuteComponentInitializing(execComponent);
            final Map<String, DataUnit> dataUnits =
                    dataUnitManager.onComponentWillExecute(execComponent);
            initialize(dataUnits);
            execute();
            execution.onExecuteComponentSuccessful(execComponent);
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

    private void execute() throws ExecutorException {
        if (instance instanceof SequentialExecution) {
            final SequentialExecution executable =
                    (SequentialExecution) instance;
            executeSequential(executable);
        } else {
            throw new ExecutorException("Unknown execution interface.");
        }
    }

    private void executeSequential(SequentialExecution executable)
            throws ExecutorException {
        final SequentialComponentExecutor executor =
                new SequentialComponentExecutor(
                        executable, execution, execComponent);
        final Thread thread = new Thread(executor, pplComponent.getLabel());
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
        final RuntimeConfiguration runtimeConfig;
        try {
            runtimeConfig = instance.getRuntimeConfiguration();
        } catch (LpException ex) {
            throw new ExecutorException("Can't get runtime configuration.", ex);
        }

        final String configGraph =
                pplComponent.getIri() + "/configuration/effective";
        final TripleWriter writer = pipeline.setConfiguration(
                pplComponent, configGraph);

        if (runtimeConfig == null) {
            Configuration.prepareConfiguration(configGraph,
                    pplComponent, null, null, writer, pipeline);
        } else {
            RdfSource runtimeSource = wrapRuntimeConfiguration(runtimeConfig);
            Configuration.prepareConfiguration(configGraph, pplComponent,
                    runtimeSource, RUNTIME_CONFIGURATION_GRAPH,
                    writer, pipeline);
        }

        try {
            instance.loadConfiguration(configGraph, pipeline.getSource());
        } catch (LpException ex) {
            throw new ExecutorException(
                    "Can't load component configuration", ex);
        }
    }

    private RdfSource wrapRuntimeConfiguration(
            RuntimeConfiguration runtimeConfiguration)
            throws ExecutorException {
        final RdfSource source = Rdf4jSource.createInMemory();
        final TripleWriter writer =
                source.getTripleWriter(RUNTIME_CONFIGURATION_GRAPH);
        try {
            runtimeConfiguration.write(writer);
            writer.flush();
        } catch (LpException | RdfUtilsException ex) {
            throw new ExecutorException(
                    "Can't copy runtime configuration.", ex);
        }
        return source;
    }


}
