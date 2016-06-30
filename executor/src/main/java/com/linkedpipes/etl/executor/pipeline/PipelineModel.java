package com.linkedpipes.etl.executor.pipeline;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.rdf.EntityLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * Store pipeline definition as loaded from RDF.
 *
 * TODO Support for debuggin.
 *
 * @author Petr Škoda
 */
public class PipelineModel implements EntityLoader.Loadable {

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
        MAP
    };

    public static class DataSource implements EntityLoader.Loadable {

        /**
         * Debug suffix.
         */
        private String debug;

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

        public String getDebug() {
            return debug;
        }

        public String getLoadPath() {
            return loadPath;
        }

        public String getExecution() {
            return execution;
        }

        @Override
        public EntityLoader.Loadable load(String predicate, Value object)
                throws LpException {
            switch (predicate) {
                case LINKEDPIPES.HAS_DEBUG:
                    this.debug = object.stringValue();
                    break;
                case LINKEDPIPES.HAS_LOAD_PATH:
                    this.loadPath = object.stringValue();
                    break;
                case LINKEDPIPES.HAS_EXECUTION:
                    this.execution = object.stringValue();
                default:
                    break;
            }
            return null;
        }

    }

    public static class DataUnit implements EntityLoader.Loadable {

        private final String iri;

        private final List<String> types = new ArrayList<>(3);

        private String binding;

        private DataSource dataSource;

        private final Component component;

        private final List<String> sources = new ArrayList<>(2);

        public DataUnit(String iri, Component component) {
            this.iri = iri;
            this.component = component;
        }

        public String getIri() {
            return iri;
        }

        public List<String> getTypes() {
            return types;
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

        public List<String> getSources() {
            return sources;
        }

        public boolean isInput() {
            return types.contains("http://linkedpipes.com/ontology/Input");
        }

        public boolean isOutput() {
            return types.contains("http://linkedpipes.com/ontology/Output");
        }

        @Override
        public EntityLoader.Loadable load(String predicate, Value object)
                throws LpException {
            switch (predicate) {
                case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type":
                    types.add(object.stringValue());
                    break;
                case LINKEDPIPES.HAS_BINDING:
                    binding = object.stringValue();
                    break;
                case LINKEDPIPES.HAS_SOURCE:
                    dataSource = new DataSource();
                    return dataSource;
                case LINKEDPIPES.HAS_PORT_SOURCE:
                    sources.add(object.stringValue());
                    return null;
            }
            return null;
        }

    }

    public static class Component implements EntityLoader.Loadable {

        private final String iri;

        private final Map<String, String> labels = new HashMap<>();

        private final List<DataUnit> dataUnits = new ArrayList<>(2);

        /**
         * Is not null if component is loaded and successfully validated.
         */
        private Integer executionOrder;

        private ExecutionType executionType;

        public Component(String iri) {
            this.iri = iri;
        }

        public String getIri() {
            return iri;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public List<DataUnit> getDataUnits() {
            return dataUnits;
        }

        public Integer getExecutionOrder() {
            return executionOrder;
        }

        public ExecutionType getExecutionType() {
            return executionType;
        }

        public String getDefaultLabel() {
            if (labels.isEmpty()) {
                return iri;
            } else if (labels.containsKey("en")) {
                return labels.get("en");
            } else if (labels.containsKey("")) {
                return labels.get("");
            } else {
                return labels.values().iterator().next();
            }
        }

        @Override
        public EntityLoader.Loadable load(String predicate, Value object)
                throws LpException {
            switch (predicate) {
                case "http://www.w3.org/2004/02/skos/core#prefLabel":
                    final Literal label = (Literal) object;
                    final String lang = label.getLanguage().orElse("");
                    labels.put(lang, label.getLabel());
                    return null;
                case LINKEDPIPES.HAS_EXECUTION_ORDER:
                    try {
                        executionOrder = Integer.parseInt(object.stringValue());
                    } catch (NumberFormatException ex) {
                        throw RdfException.invalidProperty(iri,
                                LINKEDPIPES.HAS_EXECUTION_ORDER,
                                "Must be string.", ex);
                    }
                    return null;
                case LINKEDPIPES.HAS_PORT:
                    final DataUnit newDataUnit = new DataUnit(
                            object.stringValue(), this);
                    dataUnits.add(newDataUnit);
                    return newDataUnit;
                case LINKEDPIPES.HAS_COMPONENT_EXECUTION_TYPE:
                    switch (object.stringValue()) {
                        case "http://linkedpipes.com/resources/execution/type/execute":
                            executionType = ExecutionType.EXECUTE;
                            break;
                        case "http://linkedpipes.com/resources/execution/type/mapped":
                            executionType = ExecutionType.MAP;
                            break;
                        case "http://linkedpipes.com/resources/execution/type/skip":
                            executionType = ExecutionType.SKIP;
                            break;
                        default:
                            throw RdfException.invalidProperty(iri,
                                    LINKEDPIPES.HAS_COMPONENT_EXECUTION_TYPE,
                                    "Invalid value: {}", object.stringValue());
                    }
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public void afterLoad() throws LpException {
            if (executionOrder == null || executionType == null) {
                throw RdfException.invalidProperty(iri, null,
                        "Incomplete definition.");
            }
        }

    }

    private final String iri;

    private final List<Component> components = new LinkedList<>();

    public PipelineModel(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

    public List<Component> getComponents() {
        return components;
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
    public EntityLoader.Loadable load(String predicate, Value object)
            throws LpException {
        switch (predicate) {
            case LINKEDPIPES.HAS_COMPONENT:
                final Component comp = new Component(object.stringValue());
                components.add(comp);
                return comp;
            default:
                return null;
        }
    }

}
