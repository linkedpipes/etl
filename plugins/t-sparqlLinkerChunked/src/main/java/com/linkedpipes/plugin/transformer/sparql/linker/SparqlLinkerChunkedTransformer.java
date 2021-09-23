package com.linkedpipes.plugin.transformer.sparql.linker;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkExecution;
import com.linkedpipes.etl.executor.api.v1.component.chunk.ChunkTransformer;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SparqlLinkerChunkedTransformer
        extends ChunkTransformer<ChunkedTriples.Chunk, Collection<Statement>> {

    protected final String query;

    protected final boolean isAddToChunk;

    protected final List<Statement> referenceStatements;

    protected final List<Statement> outputStatements = new ArrayList<>(10000);

    public SparqlLinkerChunkedTransformer(
            ChunkExecution<ChunkedTriples.Chunk, Collection<Statement>> owner,
            String query, boolean isAddToChunk,
            List<Statement> referenceStatements) {
        super(owner);
        this.query = query;
        this.isAddToChunk = isAddToChunk;
        this.referenceStatements = referenceStatements;
    }

    @Override
    protected Collection<Statement> processChunk(
            ChunkedTriples.Chunk chunk) throws LpException {
        outputStatements.clear();
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        Collection<Statement> statements = chunk.toCollection();
        populateRepository(repository, statements);
        executeQuery(repository);
        if (isAddToChunk) {
            outputStatements.addAll(statements);
        }
        repository.shutDown();
        return outputStatements;
    }

    protected void populateRepository(
            Repository repository,Collection<Statement> statements) {
        Repositories.consume(repository, (connection) -> {
            connection.add(statements);
            connection.add(referenceStatements);
        });
    }

    protected void executeQuery(Repository repository) {
        Repositories.consume(repository, (connection) -> {
            connection.prepareGraphQuery(query)
                    .evaluate()
                    .forEach(outputStatements::add);
        });
    }

}
