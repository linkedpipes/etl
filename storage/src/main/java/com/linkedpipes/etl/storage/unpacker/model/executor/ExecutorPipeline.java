package com.linkedpipes.etl.storage.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.BackendTripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class ExecutorPipeline {

    private String iri;

    private String label;

    private final List<ExecutorComponent> components = new LinkedList<>();

    private final List<ExecutorConnection> connections = new LinkedList<>();

    private ExecutorMetadata executorMetadata;

    private ExecutorProfile executorProfile;

    public ExecutorPipeline(String iri) {
        this.iri = iri;
        this.executorMetadata = new ExecutorMetadata(iri + "/metadata");
        this.executorProfile = new ExecutorProfile(iri + "/profile");
    }

    public void write(BackendTripleWriter writer) {
        writer.iri(iri, RDF.TYPE, LP_PIPELINE.PIPELINE);
        writer.string(iri, SKOS.PREF_LABEL, label, null);
        writer.iri(iri, LP_EXEC.HAS_METADATA, executorMetadata.getIri());
        executorMetadata.write(writer);
        writer.iri(
                iri, LP_EXEC.HAS_EXECUTION_PROFILE, executorProfile.getIri());
        executorProfile.write(writer);
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

    private void writeStatic(BackendTripleWriter writer) {
        String sesameIri = "http://localhost/repository/sesame";
        writer.iri(iri, "http://linkedpipes.com/ontology/repository",
                sesameIri);
        writer.iri(sesameIri, RDF.TYPE, LP_PIPELINE.RDF_REPOSITORY);
        writer.iri(
                sesameIri, LP_PIPELINE.HAS_REQUIREMENT,
                LP_PIPELINE.HAS_REQ_WORKING);
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

    public ExecutorProfile getExecutorProfile() {
        return executorProfile;
    }

    public String getIri() {
        return iri;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
