package com.linkedpipes.etl.executor.api.v1.dataunit;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;

/**
 * Interface for DataUnit with runtime configuration.
 */
public interface RuntimeConfiguration {

    /**
     * Write the content of a runtime configuration.
     */
    void write(TripleWriter writer) throws LpException;

}
