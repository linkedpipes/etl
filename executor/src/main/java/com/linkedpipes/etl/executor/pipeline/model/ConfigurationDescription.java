package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OBJECTS;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;

/**
 * Represent a configuration description.
 */
public class ConfigurationDescription
        implements RdfLoader.Loadable<String> {

    private final String iri;

    private String describedType;

    public ConfigurationDescription(String iri) {
        this.iri = iri;
    }

    @Override
    public RdfLoader.Loadable load(String predicate, String object)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_OBJECTS.HAS_DESCRIBE:
                describedType = object;
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
