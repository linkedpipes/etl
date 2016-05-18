package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import java.io.File;

import com.linkedpipes.etl.utils.core.entity.EntityLoader;

/**
 * Configuration class used by the RDF repository.
 *
 * @author Škoda Petr
 */
final class FactoryConfiguration implements EntityLoader.Loadable {

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
    public EntityLoader.Loadable load(String predicate, String value)
            throws EntityLoader.LoadingFailed {
        switch (predicate) {
            case LINKEDPIPES.HAS_WORKING_DIRECTORY:
                workingDirectory = value;
                return null;
            default:
                return null;
        }
    }

    @Override
    public void validate() throws EntityLoader.LoadingFailed {
        if (workingDirectory == null) {
            throw new EntityLoader.LoadingFailed(
                    "Working directory must be set!");
        }
    }

}
