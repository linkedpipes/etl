package com.linkedpipes.etl.dataunit.sesame.api.rdf;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.Statement;

import java.util.Collection;

/**
 * Write interface for chunked
 */
public interface WritableChunkedStatements {

    /**
     * Store data from the collection into data unit. The collection
     * can be cleared after this call.
     *
     * @param statements
     */
    void submit(Collection<Statement> statements) throws LpException;

}
