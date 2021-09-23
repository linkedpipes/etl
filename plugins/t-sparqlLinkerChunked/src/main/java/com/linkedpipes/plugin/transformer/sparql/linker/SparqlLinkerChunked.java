package com.linkedpipes.plugin.transformer.sparql.linker;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkExecution;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkTransformer;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Linker is designed to solve problems, where links SPARQL construct
 * is execute on two big datasets, while it can be executed in isolation
 * on their chunks.
 * <p>
 * For each reference chunk and each data chunk are put together,
 * over these data the given query is executed.
 * <p>
 * Use the same vocabulary as SPARQL construct.
 */
public final class SparqlLinkerChunked
        extends ChunkExecution<ChunkedTriples.Chunk, Collection<Statement>>
        implements Component {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlLinkerChunked.class);

    @Component.InputPort(iri = "DataRdf")
    public ChunkedTriples dataRdf;

    @Component.InputPort(iri = "ReferenceRdf")
    public SingleGraphDataUnit referenceRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.Configuration
    public SparqlConstructConfiguration configuration;

    protected List<Statement> reference = new ArrayList<>(10000);

    @Override
    public void execute(Context context) throws LpException {
        checkConfiguration();
        collectReferenceStatements();
        super.execute(context);
        reference.clear();
    }

    protected void collectReferenceStatements() throws LpException {
        referenceRdf.execute((connection) -> {
            connection.export(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st)
                        throws RDFHandlerException {
                    reference.add(st);
                }
            }, referenceRdf.getReadGraph());
        });
    }

    protected void checkConfiguration() throws LpException {
        String query = configuration.getQuery();
        if (query == null || query.isEmpty()) {
            throw new LpException("Missing query: {}",
                    SparqlConstructVocabulary.HAS_QUERY);
        }
    }

    @Override
    protected Iterator<ChunkedTriples.Chunk> chunks() throws LpException {
        return dataRdf.iterator();
    }

    @Override
    protected int getThreadCount() {
        return configuration.getThreadCount();
    }

    @Override
    protected long getChunkCount() {
        return dataRdf.size();
    }

    @Override
    protected ChunkTransformer<ChunkedTriples.Chunk, Collection<Statement>>
    createExecutor() {
        boolean isAddToChunk = SparqlConstructVocabulary.ADD_TO_CHUNK.equals(
                configuration.getOutputMode());
        return new SparqlLinkerChunkedTransformer(
                this, configuration.getQuery(), isAddToChunk, reference);
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
