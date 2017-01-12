package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

/**
 * Write interface for chunked
 */
public interface WritableChunkedTriples {

    /**
     * Store data from the collection into data unit. The collection
     * can be cleared after this call.
     *
     * @param statements
     */
    void submit(Collection<Statement> statements) throws LpException;

}
