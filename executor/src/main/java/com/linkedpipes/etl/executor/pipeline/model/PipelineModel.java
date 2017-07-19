package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Loaded from RDF represent pipeline model.
 */
public class PipelineModel implements Loadable {

    private final String iri;

    private final String graph;

    private final List<Component> components = new LinkedList<>();

    private final List<Connection> connections = new LinkedList<>();

    private final ExecutionMetadata executionMetadata = new ExecutionMetadata();

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
    public Loadable load(String predicate, RdfValue object)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_PIPELINE.HAS_COMPONENT:
                final Component component = new Component(object.asString());
                components.add(component);
                return component;
            case LP_PIPELINE.HAS_CONNECTION:
                final Connection connection = new Connection();
                connections.add(connection);
                return connection;
            case LP_PIPELINE.HAS_EXECUTION_METADATA:
                return executionMetadata;
            default:
                return null;
        }
    }

    public void afterLoad() throws InvalidPipelineException {
        check();
        sortComponents();
    }

    private void check() throws InvalidPipelineException {
        for (Component component : components) {
            component.afterLoad();
        }
    }

    private void sortComponents() {
        Collections.sort(components,
                Comparator.comparingInt(x -> x.getExecutionOrder()));
    }

    public boolean isDeleteWorkingData() {
        return executionMetadata.isDeleteWorkingData();
    }
}
