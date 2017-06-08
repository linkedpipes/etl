package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SparqlConstructExecutor implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(SparqlConstructExecutor.class);

    private final ExecutorManager manager;

    private final String query;

    private boolean failed = false;

    private List<Statement> outputBuffer = new ArrayList<>(128);

    public SparqlConstructExecutor(ExecutorManager manager, String query) {
        this.manager = manager;
        this.query = query;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!executeTask()) {
                    break;
                }
            } catch (Exception ex) {
                LOG.error("Transformation failed.", ex);
                failed = true;
                manager.terminate();
            }
        }
    }

    private boolean executeTask() throws LpException {
        Collection<Statement> statements = getInputData();
        if (statements == null) {
            return false;
        }
        Repository repository = createRepository();
        Repositories.consume(repository, (connection) -> {
            connection.add(statements);
        });
        Repositories.consume(repository, (connection) -> {
            executeQuery(connection);
        });
        manager.submitResult(outputBuffer);
        repository.shutDown();
        return true;
    }

    private Repository createRepository() {
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        return repository;
    }

    private Collection<Statement> getInputData() throws LpException {
        ChunkedTriples.Chunk chunk = manager.getChunk();
        if (chunk == null) {
            return null;
        } else {
            return chunk.toCollection();
        }
    }

    private void executeQuery(RepositoryConnection connection) {
        outputBuffer.clear();
        GraphQueryResult result =
                connection.prepareGraphQuery(query).evaluate();
        while (result.hasNext()) {
            outputBuffer.add(result.next());
        }
    }

    public boolean isFailed() {
        return failed;
    }

}
