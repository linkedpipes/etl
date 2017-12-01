package com.linkedpipes.etl.storage.unpacker.model.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.pojo.LoaderException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Execution implements Loadable {

    public static final String TYPE = LP_EXEC.EXECUTION;

    private String iri;

    private List<ExecutionComponent> components = new ArrayList<>();

    @Override
    public void resource(String resource) throws LoaderException {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, RdfValue value)
            throws RdfUtilsException {
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
