package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

/**
 * Represent a configuration description.
 */
public class ConfigurationDescription implements Loadable {

    private final String iri;

    private String describedType;

    public ConfigurationDescription(String iri) {
        this.iri = iri;
    }

    @Override
    public Loadable load(String predicate, RdfValue object) {
        switch (predicate) {
            case LP_OBJECTS.HAS_DESCRIBE:
                describedType = object.asString();
                return null;
            default:
                return null;
        }
    }

    void check() throws InvalidPipelineException {
        if (describedType == null) {
            throw new InvalidPipelineException(
                    "Missing configuration type: {}", iri);
        }
    }

    public String getIri() {
        return iri;
    }

    public String getDescribedType() {
        return describedType;
    }
}
