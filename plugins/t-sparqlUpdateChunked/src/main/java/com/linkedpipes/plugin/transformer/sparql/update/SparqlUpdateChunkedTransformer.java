package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkExecution;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkTransformer;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SparqlUpdateChunkedTransformer
        extends ChunkTransformer<ChunkedTriples.Chunk, Collection<Statement>> {

    protected final String query;

    protected List<Statement> outputBuffer = new ArrayList<>(10000);

    public SparqlUpdateChunkedTransformer(
            ChunkExecution<ChunkedTriples.Chunk, Collection<Statement>> owner,
            String query) {
        super(owner);
        this.query = query;
    }

    @Override
    protected Collection<Statement> processChunk(
            ChunkedTriples.Chunk chunk) throws LpException {
        outputBuffer.clear();
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        populateRepository(repository, chunk);
        executeQuery(repository);
        collectStatements(repository);
        return outputBuffer;
    }

    protected void populateRepository(
            Repository repository, ChunkedTriples.Chunk chunk)
            throws LpException {
        Collection<Statement> statements = chunk.toCollection();
        Repositories.consume(repository, (connection) -> {
            connection.add(statements);
        });
    }

    protected void executeQuery(Repository repository) {
        Repositories.consume(repository, (connection) -> {
            connection.prepareUpdate(query).execute();
        });
    }

    protected void collectStatements(Repository repository) throws LpException {
        Repositories.consume(repository, (connection) -> {
            connection.export(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    outputBuffer.add(st);
                }
            });
        });
        repository.shutDown();
    }

}
