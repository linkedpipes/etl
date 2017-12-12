package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;

class Configuration extends DataUnitConfiguration {

    private String workingDirectory;

    public Configuration(String iri, String graph) {
        super(iri);
    }

    @Override
    public Loadable load(String predicate, RdfValue object) {
        switch (predicate) {
            case LP_EXEC.HAS_WORKING_DIRECTORY:
                workingDirectory = object.asString();
                return null;
        }
        return super.load(predicate, object);
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

}
