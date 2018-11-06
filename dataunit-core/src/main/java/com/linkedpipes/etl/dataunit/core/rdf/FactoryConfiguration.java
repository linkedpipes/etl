package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;

import java.io.File;
import java.net.URI;

/**
 * Configuration for {@link RdfDataUnitFactory}.
 */
class FactoryConfiguration implements Loadable {

    private String directory;

    @Override
    public Loadable load(String predicate, RdfValue object) {
        switch (predicate) {
            case LP_EXEC.HAS_WORKING_DIRECTORY:
                directory = object.asString();
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * Return path to the repository directory.
     */
    public File getDirectory() {
        if (directory == null) {
            return null;
        }
        return new File(URI.create(directory));
    }

}
