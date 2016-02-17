package com.linkedpipes.etl.dataunit.sesame.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import java.util.Collection;
import java.util.LinkedList;

import com.linkedpipes.etl.utils.core.entity.EntityLoader;
import java.io.File;

/**
 * Describe common configuration of sesame data unit.
 *
 * @author Škoda Petr
 */
public final class RdfDataUnitConfiguration implements EntityLoader.Loadable {

    private final String resourceUri;

    private String binding;

    private final Collection<String> sourceDataUnitUris = new LinkedList<>();

    private final Collection<String> types = new LinkedList<>();

    private String debugDirectory;

    public RdfDataUnitConfiguration(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public RdfDataUnitConfiguration(String resourceUri, String binding, String debugDirectory) {
        this.resourceUri = resourceUri;
        this.binding = binding;
        this.debugDirectory = debugDirectory;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public String getBinding() {
        return binding;
    }

    public Collection<String> getSourceDataUnitUris() {
        return sourceDataUnitUris;
    }

    public Collection<String> getTypes() {
        return types;
    }

    public File getDebugDirectory() {
        if (debugDirectory == null) {
            return null;
        } else {
            return new File(java.net.URI.create(debugDirectory));
        }
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
                sourceDataUnitUris.add(value);
                return null;
            case LINKEDPIPES.HAS_DEBUG_DIRECTORY:
                debugDirectory = value;
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
