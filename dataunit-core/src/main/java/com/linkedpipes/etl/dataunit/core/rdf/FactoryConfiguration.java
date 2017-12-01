package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

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
        }
        return null;
    }

    /**
     * @return Path to the repository directory.
     */
    public File getDirectory() {
        if (directory == null) {
            return null;
        }
        return new File(URI.create(directory));
    }

}
