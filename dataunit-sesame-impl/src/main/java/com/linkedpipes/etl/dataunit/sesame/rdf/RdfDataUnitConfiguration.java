package com.linkedpipes.etl.dataunit.sesame.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import java.util.Collection;
import java.util.LinkedList;

import com.linkedpipes.etl.utils.core.entity.EntityLoader;

/**
 * Describe common configuration of sesame data unit.
 *
 * @author Škoda Petr
 */
public final class RdfDataUnitConfiguration implements EntityLoader.Loadable {

    /**
     * Data unit IRI.
     */
    private final String resourceIri;

    /**
     * Binding name to component property.
     */
    private String binding;

    /**
     * IRIs of source data unit.
     */
    private final Collection<String> sourceDataUnitIris = new LinkedList<>();

    /**
     * List of types.
     */
    private final Collection<String> types = new LinkedList<>();

    public RdfDataUnitConfiguration(String resourceUri) {
        this.resourceIri = resourceUri;
    }

    public RdfDataUnitConfiguration(String resourceIri, String binding) {
        this.resourceIri = resourceIri;
        this.binding = binding;
    }

    public String getResourceIri() {
        return resourceIri;
    }

    public String getBinding() {
        return binding;
    }

    public Collection<String> getSourceDataUnitIris() {
        return sourceDataUnitIris;
    }

    public Collection<String> getTypes() {
        return types;
    }

    @Override
    public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
        switch (predicate) {
            case LINKEDPIPES.HAS_BINDING:
                binding = value;
                return null;
            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type":
                types.add(value);
                return null;
            case LINKEDPIPES.HAS_PORT_SOURCE:
                sourceDataUnitIris.add(value);
                return null;
            default:
                return null;
        }
    }

    @Override
    public void validate() throws EntityLoader.LoadingFailed {
        // No operation here.
    }

}
