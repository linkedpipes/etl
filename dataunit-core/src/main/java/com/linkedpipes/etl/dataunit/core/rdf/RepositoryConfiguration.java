package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

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
