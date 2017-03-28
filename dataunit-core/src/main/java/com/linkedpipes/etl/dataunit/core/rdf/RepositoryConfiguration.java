package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;

/**
 * Configuration for the used RDF4J repository.
 */
class RepositoryConfiguration implements RdfLoader.Loadable<String> {

    private String workingDirectory;

    @Override
    public RdfLoader.Loadable load(String predicate, String object)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_EXEC.HAS_WORKING_DIRECTORY:
                workingDirectory = object;
                break;
        }
        return null;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

}
