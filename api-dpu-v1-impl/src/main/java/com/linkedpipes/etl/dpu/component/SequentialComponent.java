package com.linkedpipes.etl.dpu.component;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.extensions.ProgressReportImpl;
import com.linkedpipes.etl.dpu.extensions.FaultToleranceImpl;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.etl.dpu.extensions.ManageableExtension;
import com.linkedpipes.etl.dpu.rdf.RdfSerialization;
import com.linkedpipes.etl.dpu.rdf.RdfSerialization.CanNotDeserializeObject;
import com.linkedpipes.etl.dpu.api.extensions.AfterExecution;
import com.linkedpipes.etl.dpu.api.extensions.FaultTolerance;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
import com.linkedpipes.etl.dpu.extensions.AfterExecutionImpl;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.Component.ComponentFailed;
import com.linkedpipes.etl.executor.api.v1.component.Component.InitializationFailed;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.component.Headers;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import java.util.LinkedList;
import java.util.List;
import com.linkedpipes.etl.executor.api.v1.context.ExecutionContext;


/**
 *
 * @author Škoda Petr
 */
final class SequentialComponent implements Component {

    private static final Logger LOG = LoggerFactory.getLogger(SequentialComponent.class);

    private final SequentialExecution dpu;

    private final BundleInformation info;

    private final DpuConfiguration configuration;

    private final SparqlSelect definition;

    /**
     * Main definition graph.
     */
    private final String definitionGraph;

    /**
     * List of all instantiated extensions.
     */
    private final List<ManageableExtension> extensions = new LinkedList<>();

    SequentialComponent(SequentialExecution dpu, BundleInformation info, DpuConfiguration configuration,
            SparqlSelect definition, String graph) {
        this.dpu = dpu;
        this.info = info;
        this.configuration = configuration;
        this.definition = definition;
        this.definitionGraph = graph;
    }

    protected void bindPorts(Map<String, DataUnit> dataunits) throws InitializationFailed {
        for (Field field : dpu.getClass().getFields()) {
            final DataProcessingUnit.InputPort input = field.getAnnotation(DataProcessingUnit.InputPort.class);
            final DataProcessingUnit.OutputPort output = field.getAnnotation(DataProcessingUnit.OutputPort.class);
            if (input != null) {
                bindPort(dataunits, field, input.id(), input.optional());
            }
            if (output != null) {
                bindPort(dataunits, field, output.id(), false);
            }
        }
    }

    protected void bindPort(Map<String, DataUnit> dataUnits, Field field, String id, boolean optional)
            throws InitializationFailed {
        // Search for data unit.
        DataUnit dataUnit = null;
        for (DataUnit item : dataUnits.values()) {
            if (id.equals(item.getBinding())) {
                dataUnit = item;
            }
        }
        if (dataUnit == null) {
            if (!optional) {
                LOG.info("Expected: {}", id);
                for (DataUnit item : dataUnits.values()) {
                    LOG.info("\tFound: {}", item.getBinding());
                }
                // If it's not optional then fail for missing data unit.
                throw new InitializationFailed(String.format("Missing data unit: %s", id));
            }
        } else {
            if (field.getType().isAssignableFrom(dataUnit.getClass())) {
                try {
                    field.set(dpu, dataUnit);
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    throw new InitializationFailed(String.format("Can't set data unit: %s", id), ex);
                }
            } else {
                // Type miss match!
                LOG.error("Not assignable data units ({}): {} -> {}", id, dataUnit.getClass().getSimpleName(),
                        field.getType().getSimpleName());
                throw new InitializationFailed(String.format("Type miss match for: %s", id));
            }
        }
    }

    protected void bindExtensions(ExecutionContext context) throws InitializationFailed {
        for (Field field : dpu.getClass().getFields()) {
            if (field.getAnnotation(DataProcessingUnit.Extension.class) == null) {
                // No annotation.
                continue;
            }
            // Create extension instance.
            final ManageableExtension extension;
            if (field.getType() == FaultTolerance.class) {
                extension = new FaultToleranceImpl();
            } else if (field.getType() == ProgressReport.class) {
                extension = new ProgressReportImpl(context);
            } else if (field.getType() == AfterExecution.class) {
                extension = new AfterExecutionImpl();
            } else {
                throw new InitializationFailed("Can't initialize extension!");
            }
            extension.initialize(definition, configuration.getResourceUri(), definitionGraph);
            extensions.add(extension);
            // ...
            try {
                field.set(dpu, extension);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new InitializationFailed("Can't set extension!", ex);
            }
        }
    }

    /**
     * Load configuration to all annotated fields in DPU.
     *
     * @param runtimeConfig
     * @throws com.linkedpipes.executor.api.v1.plugin.component.Component.InitializationFailed
     */
    protected void loadConfigurations(SparqlSelect runtimeConfig) throws InitializationFailed {
        for (Field field : dpu.getClass().getFields()) {
            if (field.getAnnotation(DataProcessingUnit.Configuration.class) != null) {
                loadConfiguration(field, runtimeConfig);
            }
        }
    }

    /**
     * If this function fail or recoverable-error then only the whole function can be re-executed.
     *
     * @param field
     * @param runtimeConfig
     * @throws com.linkedpipes.executor.api.v1.plugin.component.Component.InitializationFailed
     */
    protected void loadConfiguration(Field field, SparqlSelect runtimeConfig) throws InitializationFailed {
        // Create configuration object.
        final Object fieldValue;
        try {
            fieldValue = field.getType().newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new InitializationFailed(String.format("Can't create configuration class for field: %s",
                    field.getName()), ex);
        }
        // Load configurations based on definitions.
        for (DpuConfiguration.ConfigurationHolder configurationHolder : configuration.getConfigurations()) {
            // Use current graph if no graph is given.
            final String graph = configurationHolder.getConfigurationGraph() == null
                    ? definitionGraph : configurationHolder.getConfigurationGraph();
            final String uri = configurationHolder.getConfigurationUri();
            try {
                if (uri == null) {
                    RdfSerialization.addToObject(fieldValue, definition, graph);
                } else {
                    RdfSerialization.addToObject(fieldValue, definition, graph, uri);
                }
            } catch (CanNotDeserializeObject ex) {
                throw new InitializationFailed("Can't load configuration from definition.", ex);
            }
        }
        // Load runtime configuration.
        try {
            if (runtimeConfig != null) {
                RdfSerialization.addToObject(fieldValue, runtimeConfig, null);
            }
        } catch (CanNotDeserializeObject ex) {
            throw new InitializationFailed("Can't load runtime configuration.", ex);
        }
        // Set value.
        try {
            field.set(dpu, fieldValue);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new InitializationFailed(String.format("Can't set configuration object for field: %s",
                    field.getName()), ex);
        }
    }

    /**
     * Search for configuration data unit. If it's found then try to convert it into {@link SparqlSelect} interface and
     * return it. If any of the aforementioned steps fails then return null.
     *
     * @param dataUnits
     * @return
     */
    protected SparqlSelect getConfigurationDataUnit(Map<String, DataUnit> dataUnits) {
        for (DataUnit item : dataUnits.values()) {
            if ("Configuration".equals(item.getBinding())) {
                // Try conversion to SparqlSelect.
                final SparqlSelect sparqlSelect;
                if (item instanceof SparqlSelect) {
                    sparqlSelect = (SparqlSelect)item;
                } else {
                    sparqlSelect = null;
                }
                if (sparqlSelect != null) {
                    LOG.warn("Can not wrap configuration data unit (" + item.getResourceUri()
                            + ") as a configuration source.");
                }
                return sparqlSelect;
            }
        }
        // No configuration data unit is presented.
        return null;
    }

    @Override
    public void initialize(Map<String, DataUnit> dataUnits, ExecutionContext executionContext) throws InitializationFailed {
        // Bind data units to the component.
        bindPorts(dataUnits);
        bindExtensions(executionContext);
        loadConfigurations(getConfigurationDataUnit(dataUnits));
    }

    @Override
    public void execute(ExecutionContext context) throws ComponentFailed {
        try {
            for (ManageableExtension extension : extensions) {
                extension.preExecution();
            }

            dpu.execute(new DataProcessingUnit.Context() {

                @Override
                public boolean canceled() {
                    return context.canceled();
                }

                @Override
                public String getComponentUri() {
                    return configuration.getResourceUri();
                }

                @Override
                public File getWorkingDirectory() {
                    return configuration.getWorkingDirectory();
                }

            });
        } catch (NonRecoverableException ex) {
            throw new ComponentFailed("Component failed!", ex);
        } catch (Throwable ex) {
            throw new ComponentFailed("Component failed on throwable!", ex);
        } finally {
            // In every case call postExecution methods.
            for (ManageableExtension extension : extensions) {
                extension.postExecution();
            }
        }
    }

    @Override
    public String getHeader(String key) {
        switch (key) {
            case Headers.LOG_PACKAGES:
                // Construct list of packages.
                final StringBuilder logList = new StringBuilder(64);
                final Iterator<String> iter = info.getPackages().iterator();
                logList.append(iter.next());
                while (iter.hasNext()) {
                    logList.append(",");
                    logList.append(iter.next());
                }
                LOG.debug("Log packages: {}", logList);
                return logList.toString();
            default:
                return null;
        }
    }

}
