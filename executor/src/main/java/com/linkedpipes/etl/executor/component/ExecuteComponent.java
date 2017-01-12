package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.ManageableComponent;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.dataunit.DataUnitManager;
import com.linkedpipes.etl.executor.execution.Execution;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import com.linkedpipes.etl.rdf.utils.RdfSource;

import java.util.Map;

/**
 * The component is initialized with data units and configuration in
 * the initialization phase.
 *
 * In the execution phase the execution interface is detected and
 * component is executed.
 */
class ExecuteComponent implements ComponentExecutor {

    private final Pipeline pipeline;

    private final Execution execution;

    private final PipelineModel.Component component;

    private final ManageableComponent instance;

    private final ExecutionContext context;

    public ExecuteComponent(
            Pipeline pipeline,
            Execution execution,
            PipelineModel.Component component,
            ManageableComponent instance) {
        this.pipeline = pipeline;
        this.execution = execution;
        this.component = component;
        this.instance = instance;
        //
        context = new ExecutionContext(execution,
                execution.getComponent(component));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(DataUnitManager dataUnitManager)
            throws ExecutorException {
        if (instance == null) {
            throw new ExecutorException(
                    "The component instance is null: {} ({})",
                    component.getIri(), component.isLoadInstance());
        }
        //
        final Map<String, DataUnit> dataUnits = (Map)
                dataUnitManager.onExecuteComponent(component);

        try {
            instance.initialize(dataUnits, context);
        } catch (LpException ex) {
            throw new ExecutorException("Can't initialize component.", ex);
        }
        //
        final ManageableComponent.RuntimeConfiguration runtimeConfig;
        try {
            runtimeConfig = instance.getRuntimeConfiguration();
        } catch (LpException ex) {
            throw new ExecutorException("Can't get runtime configuration.", ex);
        }
        // Prepare and load configuration.
        final String configGraph =
                component.getIri() + "/configuration/effective";
        final RdfSource.TypedTripleWriter writer = pipeline.setConfiguration(
                component, configGraph);
        if (runtimeConfig == null) {
            Configuration.prepareConfiguration(configGraph,
                    component, null, null, writer, pipeline);
        } else {
            Configuration.prepareConfiguration(configGraph, component,
                    runtimeConfig.getSource(), runtimeConfig.getGraph(),
                    writer, pipeline);
        }
        try {
            instance.loadConfiguration(configGraph, pipeline.getSource());
        } catch (LpException ex) {
            throw new ExecutorException(
                    "Can't load component configuration", ex);
        }
    }

    @Override
    public void execute() throws ExecutorException {
        if (instance instanceof SequentialExecution) {
            final SequentialExecution executable =
                    (SequentialExecution) instance;
            try {
                executable.execute();
            } catch (LpException ex) {
                throw new ExecutorException("Component execution failed.", ex);
            }
        } else {
            throw new ExecutorException("Unknown execution interface.");
        }
    }

    @Override
    public void cancel() {
        context.cancel();
    }

}
