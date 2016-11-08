package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.PojoLoader;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Describe configuration of sesame data unit.
 */
public final class RdfDataUnitConfiguration implements PojoLoader.Loadable {

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

    private String workingDirectory;

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

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public PojoLoader.Loadable load(String predicate, String value)
            throws RdfException {
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
            case LINKEDPIPES.HAS_WORKING_DIRECTORY:
                workingDirectory = value;
                return null;
            default:
                return null;
        }
    }

}
