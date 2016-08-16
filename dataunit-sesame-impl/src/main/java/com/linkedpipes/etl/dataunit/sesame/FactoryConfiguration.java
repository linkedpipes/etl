package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.PojoLoader;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

import java.io.File;

/**
 * Configuration class used by the RDF repository.
 *
 * @author Škoda Petr
 */
final class FactoryConfiguration implements PojoLoader.Loadable {

    private String workingDirectory;

    FactoryConfiguration() {
    }

    public File getWorkingDirectory() {
        return new File(java.net.URI.create(workingDirectory));
    }

    public File getRepositoryDirectory() {
        return new File(getWorkingDirectory(), "repository");
    }

    @Override
    public PojoLoader.Loadable load(String predicate, String value)
            throws RdfException {
        switch (predicate) {
            case LINKEDPIPES.HAS_WORKING_DIRECTORY:
                workingDirectory = value;
                return null;
            default:
                return null;
        }
    }

    @Override
    public void validate() throws RdfException {
        if (workingDirectory == null) {
            throw ExceptionFactory.failure("Working directory must be set!");
        }
    }

}
