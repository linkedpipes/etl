package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;

import java.util.*;

/**
 * Represent key aspects of the pipeline in form of POJO.
 */
public class PipelineModel implements RdfLoader.Loadable<String> {

    public enum ExecutionType {
        /**
         * Execute component.
         */
        EXECUTE,
        /**
         * Skip the execution and loading of data unit, in
         * fact this behave as if there was no component mentioned.
         */
        SKIP,
        /**
         * Component is mapped, so it's not executed
         * only the data units are loaded.
         */
        MAP
    }

    /**
     * Represent a source of data for given DataUnit.
     */
    public static class DataSource implements RdfLoader.Loadable<String> {

        /**
         * Path to load data from.
         */
        private String loadPath;

        /**
         * IRI of execution from which load data.
         */
        private String execution;

        public DataSource() {
        }

        public String getLoadPath() {
            return loadPath;
        }

        public String getExecution() {
            return execution;
        }

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case LP_EXEC.HAS_LOAD_PATH:
                    this.loadPath = object;
                    break;
                case LP_EXEC.HAS_EXECUTION:
                    this.execution = object;
                default:
                    break;
            }
            return null;
        }

    }

    /**
     * Represent a dataunit/port.
     */
    public static class DataUnit implements RdfLoader.Loadable<String> {

        private final String iri;

        private final List<String> types = new ArrayList<>(3);

        private String binding;

        private DataSource dataSource;

        private final Component component;

        public DataUnit(String iri, Component component) {
            this.iri = iri;
            this.component = component;
        }

        public String getIri() {
            return iri;
        }

        public String getBinding() {
            return binding;
        }

        public DataSource getDataSource() {
            return dataSource;
        }

        public Component getComponent() {
            return component;
        }

        public boolean isInput() {
            return types.contains(LP_PIPELINE.INPUT);
        }

        public boolean isOutput() {
            return types.contains(LP_PIPELINE.OUTPUT);
        }

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case RDF.TYPE:
                    types.add(object);
                    break;
                case LP_PIPELINE.HAS_BINDING:
                    binding = object;
                    break;
                case LP_EXEC.HAS_SOURCE:
                    dataSource = new DataSource();
                    return dataSource;
            }
            return null;
        }

    }

    /**
     * Represent a component configuration.
     */
    public static class Configuration implements RdfLoader.Loadable<String> {

        private final String iri;

        private Integer order;

        private String configurationGraph;

        public Configuration(String iri) {
            this.iri = iri;
        }

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case LP_EXEC.HAS_ORDER:
                    try {
                        order = Integer.parseInt(object);
                    } catch (NumberFormatException ex) {
                        throw new RdfUtilsException(
                                "Value is not an integer: {}", object);
                    }
                    return null;
                case LP_PIPELINE.HAS_CONFIGURATION_GRAPH:
                    configurationGraph = object;
                    return null;
            }
            return null;
        }

        public String getConfigurationGraph() {
            return configurationGraph;
        }

        public void afterLoad() throws ExecutorException {
            if (order == null) {
                throw new ExecutorException("Missing configuration order: {}",
                        iri);
            }
        }
    }

    /**
     * Description for a component configuration.
     */
    public static class ConfigurationDescription
            implements RdfLoader.Loadable<String> {

        private final String iri;

        private String configurationType;

        public ConfigurationDescription(String iri) {
            this.iri = iri;
        }

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case LP_OBJECTS.HAS_DESCRIBE:
                    configurationType = object;
                    return null;
                default:
                    return null;
            }
        }

        public void afterLoad() throws ExecutorException {
            if (configurationType == null) {
                throw new ExecutorException("Missing configuration type: {}",
                        iri);
            }
        }

        public String getIri() {
            return iri;
        }

        public String getConfigurationType() {
            return configurationType;
        }
    }

    /**
     * Component in a pipeline.
     */
    public static class Component implements RdfLoader.Loadable<String> {

        private final String iri;

        private String label;

        private final List<DataUnit> dataUnits = new ArrayList<>(2);

        private final List<Configuration> configurations = new ArrayList<>(3);

        /**
         * Is not null if component is loaded and successfully validated.
         */
        private Integer order;

        private ExecutionType executionType;

        private String jarPath;

        private ConfigurationDescription configurationDescription;

        public Component(String iri) {
            this.iri = iri;
        }

        public String getIri() {
            return iri;
        }

        public List<DataUnit> getDataUnits() {
            return Collections.unmodifiableList(dataUnits);
        }

        public DataUnit getDataUnit(String iri) {
            for (DataUnit dataUnit : dataUnits) {
                if (dataUnit.getIri().equals(iri)) {
                    return dataUnit;
                }
            }
            return null;
        }

        public Integer getOrder() {
            return order;
        }

        public ExecutionType getExecutionType() {
            return executionType;
        }

        public String getLabel() {
            return label;
        }

        public String getJarPath() {
            return jarPath;
        }

        public ConfigurationDescription getConfigurationDescription() {
            return configurationDescription;
        }

        /**
         * @return Ordered configuration entities for this component.
         */
        public List<Configuration> getConfigurations() {
            return configurations;
        }

        /**
         * @return True if the instance for this component should be loaded.
         */
        public boolean isLoadInstance() {
            return executionType == ExecutionType.EXECUTE;
        }

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case SKOS.PREF_LABEL:
                    label = object;
                    return null;
                case LP_EXEC.HAS_ORDER_EXEC:
                    try {
                        order = Integer.parseInt(object);
                    } catch (NumberFormatException ex) {
                        throw new RdfUtilsException(
                                "Value is not an integer: {}", object);
                    }
                    return null;
                case LP_PIPELINE.HAS_DATA_UNIT:
                    final DataUnit newDataUnit = new DataUnit(object, this);
                    dataUnits.add(newDataUnit);
                    return newDataUnit;
                case LP_EXEC.HAS_EXECUTION_TYPE:
                    switch (object) {
                        case LP_EXEC.TYPE_EXECUTE:
                            executionType = ExecutionType.EXECUTE;
                            break;
                        case LP_EXEC.TYPE_MAPPED:
                            executionType = ExecutionType.MAP;
                            break;
                        case LP_EXEC.TYPE_SKIP:
                            executionType = ExecutionType.SKIP;
                            break;
                        default:
                            throw new RdfUtilsException(
                                    "Invalid exec. type : {} {}", iri, object);
                    }
                    return null;
                case LP_EXEC.HAS_CONFIGURATION:
                    final Configuration configuration =
                            new Configuration(object);
                    configurations.add(configuration);
                    return configuration;
                case LP_PIPELINE.HAS_JAR_URL:
                    jarPath = object;
                    return null;
                case LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION:
                    configurationDescription =
                            new ConfigurationDescription(object);
                    return configurationDescription;
                default:
                    return null;
            }
        }

        public void afterLoad() throws ExecutorException {
            if (executionType == null) {
                throw new ExecutorException("Missing execution type: {}", iri);
            } else if (executionType == ExecutionType.SKIP) {
                // As the component is not executed we do not need
                // to validate the definition any more.
                return;
            }
            if (order == null) {
                throw new ExecutorException("Missing execution order: {}", iri);
            }
            if (configurationDescription == null) {
                throw new ExecutorException(
                        "Missing configuration description: {} jar: {}",
                        iri, jarPath);
            }
            configurationDescription.afterLoad();
            for (Configuration configuration : configurations) {
                configuration.afterLoad();
            }
            //
            Collections.sort(configurations, (left, right) -> {
                return Integer.compare(right.order, left.order);
            });
        }
    }

    /**
     * Connection between components.
     */
    public static class Connection implements RdfLoader.Loadable<String> {

        private String sourceComponent;

        private String sourceBinding;

        private String targetComponent;

        private String targetBinding;

        @Override
        public RdfLoader.Loadable load(String predicate, String object)
                throws RdfUtilsException {
            switch (predicate) {
                case LP_PIPELINE.HAS_SOURCE_BINDING:
                    sourceBinding = object;
                    break;
                case LP_PIPELINE.HAS_SOURCE_COMPONENT:
                    sourceComponent = object;
                    break;
                case LP_PIPELINE.HAS_TARGET_BINDING:
                    targetBinding = object;
                    break;
                case LP_PIPELINE.HAS_TARGET_COMPONENT:
                    targetComponent = object;
                    break;
            }
            return null;
        }

        public String getSourceComponent() {
            return sourceComponent;
        }

        public String getSourceBinding() {
            return sourceBinding;
        }

        public String getTargetComponent() {
            return targetComponent;
        }

        public String getTargetBinding() {
            return targetBinding;
        }
    }

    private final String iri;

    private final String graph;

    private final List<Component> components = new LinkedList<>();

    private final List<Connection> connections = new LinkedList<>();

    public PipelineModel(String iri, String graph) {
        this.iri = iri;
        this.graph = graph;
    }

    public String getIri() {
        return iri;
    }

    public String getGraph() {
        return graph;
    }

    public List<Component> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public List<Connection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    public Component getComponent(String iri) {
        for (Component component : components) {
            if (component.getIri().equals(iri)) {
                return component;
            }
        }
        return null;
    }

    @Override
    public RdfLoader.Loadable load(String predicate, String object)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_PIPELINE.HAS_COMPONENT:
                final Component component = new Component(object);
                components.add(component);
                return component;
            case LP_PIPELINE.HAS_CONNECTION:
                final Connection connection = new Connection();
                connections.add(connection);
                return connection;
            default:
                return null;
        }
    }

    public void afterLoad() throws ExecutorException {
        for (Component component : components) {
            component.afterLoad();
        }
        Collections.sort(components, Comparator.comparingInt(x -> x.order));
    }

    public boolean isDataUnitUsed(Component component, DataUnit dataUnit)
            throws ExecutorException {
        switch (component.getExecutionType()) {
            case EXECUTE:
                return true;
            case SKIP:
                return false;
            case MAP:
                break;
            default:
                throw new ExecutorException("Invalid execution type: {} ",
                        component.getExecutionType());
        }
        // MAP
        if (dataUnit.isInput()) {
            return false;
        }
        for (Connection connection : findConnections(component, dataUnit)) {
            final Component source =
                    getComponent(connection.getSourceComponent());
            if (source.getExecutionType() == ExecutionType.EXECUTE) {
                return true;
            }
            final Component target =
                    getComponent(connection.getTargetComponent());
            if (target.getExecutionType() == ExecutionType.EXECUTE) {
                return true;
            }
        }
        return true;
    }

    private Collection<Connection> findConnections(
            Component component, DataUnit dataUnit) {
        final Collection<Connection> output = new LinkedList<>();
        for (Connection connection : connections) {
            if (connection.getSourceComponent().equals(component.getIri()) &&
                    connection.getSourceBinding().equals(dataUnit.getIri())) {
                output.add(connection);
                continue;
            }
            if (connection.getTargetComponent().equals(component.getIri()) &&
                    connection.getTargetBinding().equals(dataUnit.getIri())) {
                output.add(connection);
                continue;
            }
        }
        return output;
    }

}
