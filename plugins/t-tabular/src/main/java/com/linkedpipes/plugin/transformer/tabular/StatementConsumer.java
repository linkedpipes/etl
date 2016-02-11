package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
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

    public void onRowEnd() throws NonRecoverableException;

    public void onFileStart() throws NonRecoverableException;

    public void onFileEnd() throws NonRecoverableException;

    public void submit(Resource subject, IRI predicate, Value object) throws NonRecoverableException;

}
