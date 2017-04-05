package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.dataunit.core.BaseConfiguration;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

/**
 * Configuration entity for DataUnit instance.
 */
class Configuration extends BaseConfiguration {

    private String workingDirectory;

    public Configuration(String iri, String graph) {
        super(iri, graph);
    }

    @Override
    public Loadable load(String predicate, RdfValue object) {
        switch (predicate) {
            case LP_EXEC.HAS_WORKING_DIRECTORY:
                workingDirectory = object.asString();
                break;
        }
        return super.load(predicate, object);
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

}
