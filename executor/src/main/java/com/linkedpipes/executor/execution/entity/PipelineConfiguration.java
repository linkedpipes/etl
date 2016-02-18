package com.linkedpipes.executor.execution.entity;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.linkedpipes.etl.utils.core.entity.EntityLoader;

/**
 * Represent a java version of stored definition.
 *
 * @author Škoda Petr
 */
public class PipelineConfiguration implements EntityLoader.Loadable {

    /**
     * Contains basic information required by core about data unit.
     */
    public static class DataUnit implements EntityLoader.Loadable {

        /**
         * Id used by component to identify this data unit.
         */
        private String name;

        /**
         * Unique name in scope of pipeline execution.
         */
        private final String executionId;

        private final String uri;

        public DataUnit(String executionId, String uri) {
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

        @Override
        public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
            switch (predicate) {
                case LINKEDPIPES.HAS_BINDING:
                    name = value;
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

        /**
         * Unique name in scope of pipeline execution.
         */
        private final String executionId;

        private String label;

        private final String uri;

        private final List<DataUnit> dataUnits = new ArrayList<>(3);

        private Integer executionOrder;

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
                    final DataUnit newDataUnit
                            = new DataUnit(executionId + "-" + Integer.toString(dataUnits.size()), value);
                    dataUnits.add(newDataUnit);
                    return newDataUnit;
                default:
                    return null;

            }
        }

        @Override
        public void validate() throws EntityLoader.LoadingFailed {
            if (executionOrder == null) {
                throw new EntityLoader.LoadingFailed("Execution order must be set! (uri: '" + uri + "' )");
            }
        }

    }

    private final String uri;

    private String label;

    /**
     * List of components subjects.
     */
    private final List<Component> components = new LinkedList<>();

    public PipelineConfiguration(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public String getLabel() {
        return label;
    }

    public List<Component> getComponents() {
        return components;
    }

    @Override
    public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
        switch (predicate) {
            case "http://www.w3.org/2004/02/skos/core#prefLabel":
                    label = value;
                    return null;
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
