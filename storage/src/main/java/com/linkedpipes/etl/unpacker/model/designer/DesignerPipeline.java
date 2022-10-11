package com.linkedpipes.etl.unpacker.model.designer;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.model.vocabulary.SKOS;
import com.linkedpipes.etl.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.util.LinkedList;
import java.util.List;

public class DesignerPipeline implements Loadable {

    public static final String TYPE = LP_PIPELINE.PIPELINE;

    private String iri;

    private String label;

    private int version = 0;

    private final ExecutionProfile executionProfile = new ExecutionProfile();

    private final List<DesignerConnection> connections = new LinkedList<>();

    private final List<DesignerComponent> components = new LinkedList<>();

    private final List<DesignerRunAfter> runAfter = new LinkedList<>();

    public DesignerPipeline() {
    }

    @Override
    public void resource(String resource) {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, Value value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_PROFILE:
                return executionProfile;
            case SKOS.PREF_LABEL:
                label = value.stringValue();
                return null;
            case LP_PIPELINE.HAS_VERSION:
                if (value instanceof Literal literal) {
                    version = literal.intValue();
                }
                return null;
            default:
                return null;
        }
    }

    public String getIri() {
        return iri;
    }

    public String getLabel() {
        return label;
    }

    public int getVersion() {
        return version;
    }

    public List<DesignerConnection> getConnections() {
        return connections;
    }

    public List<DesignerRunAfter> getRunAfter() {
        return runAfter;
    }

    public List<DesignerComponent> getComponents() {
        return components;
    }

    public DesignerComponent getComponent(String componentIri) {
        for (DesignerComponent component : components) {
            if (component.getIri().equals(componentIri)) {
                return component;
            }
        }
        return null;
    }

    public ExecutionProfile getExecutionProfile() {
        return executionProfile;
    }

}
