package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

/**
 * Consumes triples and store then into output.
 */
public interface StatementConsumer {

    void onRowStart();

    void onRowEnd() throws LpException;

    void onFileStart() throws LpException;

    void onFileEnd() throws LpException;

    void submit(Resource subject, IRI predicate, Value object)
            throws LpException;

}
