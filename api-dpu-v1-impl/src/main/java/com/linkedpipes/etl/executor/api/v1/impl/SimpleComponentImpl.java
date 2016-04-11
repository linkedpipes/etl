package com.linkedpipes.etl.executor.api.v1.impl;

import com.linkedpipes.etl.dpu.api.Component;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.etl.executor.api.v1.impl.RdfReader.CanNotDeserializeObject;
import com.linkedpipes.etl.dpu.api.service.AfterExecution;
import com.linkedpipes.etl.dpu.api.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.dpu.api.executable.SimpleExecution;
import com.linkedpipes.etl.executor.api.v1.component.BaseComponent;
import com.linkedpipes.etl.executor.api.v1.component.SimpleComponent;

/**
 *
 * @author Škoda Petr
 */
final class SimpleComponentImpl implements SimpleComponent {

    private static final Logger LOG
            = LoggerFactory.getLogger(SimpleComponentImpl.class);

    /**
     * Instance of a DPU code to execute.
     */
    private final SimpleExecution component;

    /**
     * Bundle information about the DPU.
     */
    private final BundleInformation info;

    /**
     * RDF configuration about this DPU.
     */
    private final ComponentConfiguration configuration;

    /**
     * Reference to the definition class.
     */
    private final SparqlSelect definition;

    /**
     * Main definition graph.
     */
    private final String definitionGraph;

    /**
     * After execution object if used.
     */
    private AfterExecutionImpl afterExecution = null;

    /**
     * Component context.
     */
    private Context context = null;

    SimpleComponentImpl(SimpleExecution dpu, BundleInformation info,
            ComponentConfiguration configuration, SparqlSelect definition,
            String graph) {
        this.component = dpu;
        this.info = info;
        this.configuration = configuration;
        this.definition = definition;
        this.definitionGraph = graph;
    }

    /**
     * Bind given data units to the port of the {@link #component}.
     *
     * @param dataunits
     * @throws Component.InitializationFailed
     */
    protected void bindPorts(Map<String, DataUnit> dataunits)
            throws InitializationFailed {
        for (Field field : component.getClass().getFields()) {
            final Component.InputPort input
                    = field.getAnnotation(Component.InputPort.class);
            final Component.OutputPort output
                    = field.getAnnotation(Component.OutputPort.class);
            if (input != null) {
                bindPort(dataunits, field, input.id(), input.optional());
            }
            if (output != null) {
                bindPort(dataunits, field, output.id(), false);
            }
        }
    }

    protected void bindPort(Map<String, DataUnit> dataUnits, Field field,
            String id, boolean optional) throws InitializationFailed {
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
                throw new InitializationFailed("Missing data unit: {}", id);
            }
        } else if (field.getType().isAssignableFrom(dataUnit.getClass())) {
            try {
                field.set(component, dataUnit);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new InitializationFailed("Can't set data unit: {}", id,
                        ex);
            }
        } else {
            // Type miss match!
            LOG.error("Not assignable data units ({}): {} -> {}", id,
                    dataUnit.getClass().getSimpleName(),
                    field.getType().getSimpleName());
            throw new InitializationFailed("Type miss match for: {}", id);
        }
    }

    protected void injectObjects(BaseComponent.Context context)
            throws InitializationFailed {
        for (Field field : component.getClass().getFields()) {
            if (field.getAnnotation(Component.Inject.class) == null) {
                // No annotation.
                continue;
            }
            // Create extension instance.
            Object object;
            if (field.getType() == ProgressReport.class) {
                object = new ProgressReportImpl(context,
                        configuration.getResourceIri());
            } else if (field.getType() == AfterExecution.class) {
                afterExecution = new AfterExecutionImpl();
                object = afterExecution;
            } else {
                throw new InitializationFailed("Can't initialize extension!");
            }
            // ...
            try {
                field.set(component, object);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new InitializationFailed("Can't set extension!", ex);
            }
        }
    }

    /**
     * Load configuration to all annotated fields in DPU.
     *
     * @param runtimeConfig
     * @throws Component.InitializationFailed
     */
    protected void loadConfigurations(SparqlSelect runtimeConfig)
            throws InitializationFailed {
        for (Field field : component.getClass().getFields()) {
            if (field.getAnnotation(Component.Configuration.class) != null) {
                loadConfiguration(field, runtimeConfig);
            }
        }
    }

    /**
     * If this function fail or recoverable-error then only the whole
     * function can be re-executed.
     *
     * @param field
     * @param runtimeConfig
     * @throws Component.InitializationFailed
     */
    protected void loadConfiguration(Field field, SparqlSelect runtimeConfig)
            throws InitializationFailed {
        // Create configuration object.
        final Object fieldValue;
        try {
            fieldValue = field.getType().newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new InitializationFailed(
                    "Can't create configuration class for field: {}",
                    field.getName(), ex);
        }
        // Load configurations from definitions.
        for (ComponentConfiguration.Configuration configRef
                : configuration.getConfigurations()) {
            // Use current graph if no graph is given.
            final String graph = configRef.getConfigurationGraph() == null
                    ? definitionGraph : configRef.getConfigurationGraph();
            final String uri = configRef.getConfigurationIri();
            try {
                if (uri == null) {
                    RdfReader.addToObject(fieldValue, definition, graph);
                } else {
                    RdfReader.addToObject(fieldValue, definition, graph,
                            uri);
                }
            } catch (CanNotDeserializeObject ex) {
                throw new InitializationFailed(
                        "Can't load configuration from definition.", ex);
            }
        }
        // Load runtime configuration.
        try {
            if (runtimeConfig != null) {
                RdfReader.addToObject(fieldValue, runtimeConfig, null);
            }
        } catch (CanNotDeserializeObject ex) {
            throw new InitializationFailed(
                    "Can't load runtime configuration.", ex);
        }
        // Set value.
        try {
            field.set(component, fieldValue);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new InitializationFailed(
                    "Can't set configuration object for field: {}",
                    field.getName(), ex);
        }
    }

    /**
     * Search for configuration data unit. If it's found then try to convert
     * it into {@link SparqlSelect} interface and return it.
     *
     * Must be called after all the data units are bound.
     *
     * @param dataUnits
     * @return Null if there is no configuration data unit.
     */
    private SparqlSelect getConfigurationDataUnit(
            Map<String, DataUnit> dataUnits) throws InitializationFailed {

        for (Field field : component.getClass().getFields()) {
            final Component.ContainsConfiguration config
                    = field.getAnnotation(Component.ContainsConfiguration.class);
            if (config != null) {
                final Object value;
                try {
                    value = field.get(component);
                } catch (Exception ex) {
                    throw new InitializationFailed("Can't read field.", ex);
                }
                final SparqlSelect sparqlSelect;
                if (SparqlSelect.class.isAssignableFrom(value.getClass())) {
                    sparqlSelect = (SparqlSelect) value;
                } else {
                    sparqlSelect = null;
                }
                if (sparqlSelect != null) {
                    LOG.warn("Can not used data unit"
                            + " ({}) as a configuration source.",
                            field.getName());
                }
                return sparqlSelect;
            }
        }

        for (DataUnit item : dataUnits.values()) {
            if ("Configuration".equals(item.getBinding())) {
                // Try conversion to SparqlSelect.
                final SparqlSelect sparqlSelect;
                if (item instanceof SparqlSelect) {
                    sparqlSelect = (SparqlSelect) item;
                } else {
                    sparqlSelect = null;
                }
                if (sparqlSelect != null) {
                    LOG.warn("Can not wrap configuration data unit"
                            + " ({}) as a configuration source.",
                            item.getResourceIri());
                }
                return sparqlSelect;
            }
        }

        // No configuration data unit is presented.
        return null;
    }

    @Override
    public void initialize(Map<String, DataUnit> dataUnits, Context context)
            throws InitializationFailed {
        this.context = context;
        bindPorts(dataUnits);
        injectObjects(context);
        // Must be called after bindPorts.
        loadConfigurations(getConfigurationDataUnit(dataUnits));
    }

    @Override
    public void execute() throws ComponentFailed {
        try {
            component.execute(new Component.Context() {

                @Override
                public boolean canceled() {
                    return context.canceled();
                }

                @Override
                public String getComponentIri() {
                    return configuration.getResourceIri();
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
            if (afterExecution != null) {
                afterExecution.postExecution();
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
