package com.linkedpipes.etl.storage.unpacker.model.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TemplatePort implements Loadable {

    private final List<String> types = new LinkedList<>();

    private String binding;

    private final List<String> requirements = new LinkedList<>();

    public TemplatePort() {
    }

    @Override
    public Loadable load(String predicate, BackendRdfValue value) {
        switch (predicate) {
            case RDF.TYPE:
                types.add(value.asString());
                return null;
            case LP_PIPELINE.HAS_BINDING:
                binding = value.asString();
                return null;
            case LP_PIPELINE.HAS_REQUIREMENT:
                requirements.add(value.asString());
                return null;
            default:
                return null;
        }
    }

    public List<String> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public String getBinding() {
        return binding;
    }

    public List<String> getRequirements() {
        return Collections.unmodifiableList(requirements);
    }

}
