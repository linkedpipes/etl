package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

/**
 * Consumes triples and store then into output.
 *
 * @author Petr Škoda
 */
public interface StatementConsumer {

    public void onRowStart();

    public void onRowEnd() throws LpException;

    public void onFileStart() throws LpException;

    public void onFileEnd() throws LpException;

    public void submit(Resource subject, IRI predicate, Value object) throws LpException;

}
