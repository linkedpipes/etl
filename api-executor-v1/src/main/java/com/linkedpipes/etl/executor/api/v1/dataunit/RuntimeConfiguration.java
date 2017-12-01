package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;

/**
 * Interface for runtime configuration.
 */
public interface RuntimeConfiguration {

    /**
     * Write the content of a runtime configuration.
     *
     * @param writer
     */
    void write(TripleWriter writer) throws LpException;

}
