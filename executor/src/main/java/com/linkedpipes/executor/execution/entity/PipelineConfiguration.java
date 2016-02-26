package com.linkedpipes.executor.execution.entity;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.linkedpipes.etl.utils.core.entity.EntityLoader;
import com.linkedpipes.etl.utils.core.entity.EntityLoader.Loadable;
import com.linkedpipes.etl.utils.core.entity.EntityLoader.LoadingFailed;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration.Component.ExecutionType;

/**
 * Represent a java version of stored definition.
 *
 * @author Škoda Petr
 */
public class PipelineConfiguration implements EntityLoader.Loadable {

    /**
     * Represent a data sourceData, that can be loaded into a data unit.
     */
    public static class DataSource implements EntityLoader.Loadable {

        /**
         * Path to data sourceData save directory.
         */
        private String loadPath;

        public String getLoadPath() {
            return loadPath;
        }

        @Override
        public Loadable load(String predicate, String value) throws LoadingFailed {
            switch (predicate) {
                case LINKEDPIPES.HAS_LOAD_PATH:
                    this.loadPath = value;
                    return null;
                case LINKEDPIPES.HAS_DEBUG_PATH:
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public void validate() throws LoadingFailed {
            // No operation here.
        }

    }

    /**
     * Contains basic information required by core about data unit.
     */
    public static class DataUnit implements EntityLoader.Loadable {

        private List<String> types  = new ArrayList<>(3);

        /**
         * Id used by component to identify this data unit.
         */
        private String name;

        /**
         * Unique name in scope of pipeline execution.
         */
        private final String executionId;

        /**
         * Resource URI.
         */
        private final String uri;

        /**
         * If set, referenced content should be used as a content for this data unit.
         */
        private DataSource sourceData = null;

        /**
         * List of source data units IRIs.
         */
        private List<String> sourceDataUnits = new ArrayList<>(1);

        /**
         * URI fragment used to create a debug IRI for the data unit.
         */
        private String uriFragment;

        /**
         * Owner component.
         */
        private Component component;

        public DataUnit(Component component, String executionId, String uri) {
            this.executionId = executionId;
            this.uri = uri;
        }

        public String getName() {
            return name;
        }

        public String getExecutionId() {
            return executionId;
        }

        public String getUri() {
            return uri;
        }

        public DataSource getSourceData() {
            return sourceData;
        }

        public List<String> getSourceDataUnits() {
            return sourceDataUnits;
        }

        public String getUriFragment() {
            return uriFragment;
        }

        public Component getComponent() {
            return component;
        }

        public boolean isInput() {
            return types.contains("http://linkedpipes.com/ontology/Input");
        }

        public boolean isOutput() {
            return types.contains("http://linkedpipes.com/ontology/Output");
        }

        @Override
        public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
            switch (predicate) {
                case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type":
                    types.add(value);
                    return null;
                case LINKEDPIPES.HAS_BINDING:
                    name = value;
                    return null;
                case LINKEDPIPES.HAS_SOURCE:
                    sourceData = new DataSource();
                    return sourceData;
                case LINKEDPIPES.HAS_PORT_SOURCE:
                    sourceDataUnits.add(value);
                    return null;
                case LINKEDPIPES.HAS_URI_FRAGMENT:
                    uriFragment = value;
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public void validate() throws EntityLoader.LoadingFailed {
            if (name == null) {
                throw new EntityLoader.LoadingFailed(String.format("Missing id for: %s", uri));
            }
        }

    }

    /**
     * Basic description of a component.
     */
    public static class Component implements EntityLoader.Loadable {

        public static enum ExecutionType {
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
            MAPPED
        };

        /**
         * Unique name in scope of pipeline execution.
         */
        private final String executionId;

        /**
         * Primary label.
         */
        private String label;

        /**
         * Resource URI.
         */
        private final String uri;

        /**
         * Informations from ports, ie. dataunits.
         */
        private final List<DataUnit> dataUnits = new ArrayList<>(3);

        /**
         * Execution order.
         */
        private Integer executionOrder;

        private ExecutionType executionType;

        public Component(String uri, String executionId) {
            this.uri = uri;
            this.executionId = executionId;
        }

        public String getExecutionId() {
            return executionId;
        }

        public String getUri() {
            return uri;
        }

        public List<DataUnit> getDataUnits() {
            return dataUnits;
        }

        public int getExecutionOrder() {
            return executionOrder;
        }

        public String getLabel() {
            if (label == null) {
                return "execId:" + executionId;
            } else {
                return label;
            }
        }

        public ExecutionType getExecutionType() {
            return executionType;
        }

        @Override
        public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
            switch (predicate) {
                case "http://www.w3.org/2004/02/skos/core#prefLabel":
                    label = value;
                    return null;
                case LINKEDPIPES.HAS_EXECUTION_ORDER:
                    try {
                        executionOrder = Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        throw new EntityLoader.LoadingFailed("Execution order must be an integer!", ex);
                    }
                    return null;
                case LINKEDPIPES.HAS_PORT:
                    final String iri = executionId + "-" + Integer.toString(dataUnits.size());
                    final DataUnit newDataUnit = new DataUnit(this, iri, value);
                    dataUnits.add(newDataUnit);
                    return newDataUnit;
                case LINKEDPIPES.HAS_EXECUTION_TYPE:
                    switch (value) {
                        case "http://linkedpipes.com/resources/execution/type/execute":
                            executionType = ExecutionType.EXECUTE;
                            break;
                        case "http://linkedpipes.com/resources/execution/type/mapped":
                            executionType = ExecutionType.MAPPED;
                            break;
                        case "http://linkedpipes.com/resources/execution/type/skip":
                            executionType = ExecutionType.SKIP;
                            break;
                        default:
                            throw new LoadingFailed("Invalid value of executionType: {}", value);
                    }
                    return null;
                default:
                    return null;

            }
        }

        @Override
        public void validate() throws EntityLoader.LoadingFailed {
            if (executionOrder == null) {
                throw new EntityLoader.LoadingFailed("Execution order must be set. (component: {})", uri);
            }
            if (executionType == null) {
                throw new LoadingFailed("Execution type must be set. (component: {})", uri);
            }
        }

    }

    /**
     * Pipeline URI.
     */
    private final String uri;

    /**
     * List of components to execute.
     */
    private final List<Component> components = new LinkedList<>();

    public PipelineConfiguration(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public List<Component> getComponents() {
        return components;
    }

    public Component getComponent(String iri) {
        for (Component component : components) {
            if (component.getUri().equals(iri)) {
                return component;
            }
        }
        return null;
    }

    /**
     *
     * @param iri
     * @return True if data unit is used during the execution.
     */
    public boolean isDataUnitUsed(String iri) {
        // Component must be used by executing component or be sourceData for such component.
        for (Component component : components) {
            if (component.getExecutionType() == ExecutionType.EXECUTE) {
                for (DataUnit dataUnit : component.getDataUnits()) {
                    if (dataUnit.uri.equals(iri) || dataUnit.getSourceDataUnits().contains(iri)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
        switch (predicate) {
            case LINKEDPIPES.HAS_COMPONENT:
                final Component newComponent = new Component(value, Integer.toString(components.size()));
                components.add(newComponent);
                return newComponent;
            default:
                return null;
        }
    }

    @Override
    public void validate() throws EntityLoader.LoadingFailed {
        if (components.isEmpty()) {
            throw new EntityLoader.LoadingFailed("Pipeline definition must contains at least one component.");
        }
        // Perform sort on components.
        components.sort((left, right) -> {
            if (left.getExecutionOrder() < right.getExecutionOrder()) {
                return -1;
            } else if (left.getExecutionOrder() > right.getExecutionOrder()) {
                return 1;
            } else {
                return 0;
            }
        });
    }

}
