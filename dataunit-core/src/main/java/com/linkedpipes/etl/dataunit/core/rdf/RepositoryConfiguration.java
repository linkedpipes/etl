package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;

/**
 * Configuration for the used RDF4J repository.
 */
class RepositoryConfiguration implements Loadable {

    private String workingDirectory;

    @Override
    public Loadable load(String predicate, RdfValue object) {
        switch (predicate) {
            case LP_EXEC.HAS_WORKING_DIRECTORY:
                workingDirectory = object.asString();
                break;
        }
        return null;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

}
