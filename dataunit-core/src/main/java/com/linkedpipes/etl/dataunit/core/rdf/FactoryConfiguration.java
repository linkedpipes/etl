package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;

import java.io.File;
import java.net.URI;

/**
 * Configuration for {@link RdfDataUnitFactory}.
 */
class FactoryConfiguration implements RdfLoader.Loadable<String> {

    private String directory;

    @Override
    public RdfLoader.Loadable load(String predicate, String object)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_EXEC.HAS_WORKING_DIRECTORY:
                directory = object;
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
