package com.linkedpipes.etl.storage.unpacker.model.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Execution implements Loadable {

    public static final String TYPE = LP_EXEC.EXECUTION;

    private String iri;

    private List<ExecutionComponent> components = new ArrayList<>();

    @Override
    public void resource(String resource) {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, Value value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_COMPONENT:
                ExecutionComponent newComponent = new ExecutionComponent();
                components.add(newComponent);
                return newComponent;
            default:
                return null;
        }
    }

    public String getIri() {
        return iri;
    }

    public List<ExecutionComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public ExecutionComponent getComponent(String iri) {
        for (ExecutionComponent component : components) {
            if (component.getIri().equals(iri)) {
                return component;
            }
        }
        return null;
    }

}
