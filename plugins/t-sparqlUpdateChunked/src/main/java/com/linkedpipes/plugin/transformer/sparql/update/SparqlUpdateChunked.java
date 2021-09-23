package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkExecution;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkTransformer;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;
import java.util.Iterator;

/**
 * Chunked version of SparqlUpdate.
 */
public final class SparqlUpdateChunked
        extends ChunkExecution<ChunkedTriples.Chunk, Collection<Statement>>
        implements Component {

    @Component.InputPort(iri = "InputRdf")
    public ChunkedTriples inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.Configuration
    public SparqlUpdateConfiguration configuration;

    @Override
    protected Iterator<ChunkedTriples.Chunk> chunks() {
        return inputRdf.iterator();
    }

    @Override
    protected int getThreadCount() {
        return configuration.getThreadCount();
    }

    @Override
    protected long getChunkCount() {
        return inputRdf.size();
    }

    @Override
    protected ChunkTransformer<ChunkedTriples.Chunk, Collection<Statement>>
    createExecutor() {
        return new SparqlUpdateChunkedTransformer(this, configuration.getQuery());
    }

    @Override
    protected boolean shouldSkipFailures() {
        return configuration.isSkipOnFailure();
    }

    @Override
    protected void submitInternal(Collection<Statement> statements)
            throws LpException {
        outputRdf.submit(statements);
        statements.clear();
    }

}
