package com.linkedpipes.etl.storage.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;

import java.util.*;

public class ExecutorPipeline {

    private String iri;

    private String label;

    private final List<ExecutorComponent> components = new LinkedList<>();

    private final List<ExecutorConnection> connections = new LinkedList<>();

    private ExecutorMetadata executorMetadata = null;

    public ExecutorPipeline(String iri) {
        this.iri = iri;
        this.executorMetadata = new ExecutorMetadata(iri + "/metadata");
    }

    public void write(TripleWriter writer) {
        writer.iri(iri, RDF.TYPE, LP_PIPELINE.PIPELINE);
        writer.string(iri, SKOS.PREF_LABEL, label, null);
        writer.iri(iri, LP_EXEC.HAS_METADATA, executorMetadata.getIri());
        executorMetadata.write(writer);
        for (ExecutorComponent component : components) {
            writer.iri(iri, LP_PIPELINE.HAS_COMPONENT, component.getIri());
            component.write(writer);
        }
        for (ExecutorConnection connection : connections) {
            writer.iri(iri, LP_PIPELINE.HAS_CONNECTION, connection.getIri());
            connection.write(writer);
        }
        writeStatic(writer);
    }

    private void writeStatic(TripleWriter writer) {
        String sesameIri = "http://localhost/repository/sesame";
        writer.iri(iri, "http://linkedpipes.com/ontology/repository",
                sesameIri);
        // TODO Move to vocabulary
        writer.iri(sesameIri, RDF.TYPE,
                "http://linkedpipes.com/ontology/dataUnit/sesame/1.0/Repository");
        writer.iri(sesameIri, "http://linkedpipes.com/ontology/requirement",
                "http://linkedpipes.com/resources/requirement/workingDirectory");
    }

    /**
     * Required component execution type to be set.
     */
    public Collection<String> getReferencedGraphs() {
        Collection<String> graphs = new HashSet<>();
        for (ExecutorComponent component : components) {
            if (!LP_EXEC.TYPE_EXECUTE.equals(component.getExecutionType())) {
                continue;
            }
            if (component.getConfigGraph() != null) {
                graphs.add(component.getConfigGraph());
            }
            if (component.getConfigDescriptionGraph() != null) {
                graphs.add(component.getConfigDescriptionGraph());
            }
        }
        return graphs;
    }

    public void addComponent(ExecutorComponent component) {
        components.add(component);
    }

    public ExecutorComponent getComponent(String iri) {
        for (ExecutorComponent component : components) {
            if (component.getIri().equals(iri)) {
                return component;
            }
        }
        return null;
    }

    public List<ExecutorComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public void addConnection(ExecutorConnection connection) {
        connections.add(connection);
    }

    public List<ExecutorConnection> getConnections() {
        return connections;
    }

    public ExecutorMetadata getExecutorMetadata() {
        return executorMetadata;
    }

    public String getIri() {
        return iri;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
