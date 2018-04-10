package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

class SparqlConstructExecutor implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(SparqlConstructExecutor.class);

    private final Map<String,String> contextMap = MDC.getCopyOfContextMap();

    private final ExecutorManager manager;

    private final String query;

    private boolean failed = false;

    private List<Statement> outputBuffer = new ArrayList<>(128);

    private final boolean deduplicateResults;

    private final boolean isSoftFail;

    private ChunkedTriples.Chunk chunk;

    public SparqlConstructExecutor(ExecutorManager manager, String query,
            boolean deduplicateResults, boolean isSoftFail) {
        this.manager = manager;
        this.query = query;
        this.deduplicateResults = deduplicateResults;
        this.isSoftFail = isSoftFail;
    }

    @Override
    public void run() {
        MDC.setContextMap(contextMap);
        LOG.info("Executor is running ...");
        while (true) {
            try {
                if (!executeTask()) {
                    break;
                }
            } catch (Exception ex) {
                LOG.error("Transformation failed for: {}", this.chunk, ex);
                if (!this.isSoftFail) {
                    failed = true;
                    manager.terminate();
                }
            }
        }
        LOG.info("Executor is running ... done");
    }

    private boolean executeTask() throws LpException {
        Collection<Statement> statements = getInputData();
        if (statements == null) {
            return false;
        }
        LOG.info("Executing task (size: {}) ...", statements.size());
        Repository repository = createRepository();
        Repositories.consume(repository, (connection) -> {
            connection.add(statements);
        });
        Repositories.consume(repository, (connection) -> {
            executeQuery(connection);
        });
        repository.shutDown();
        manager.submitResult(outputBuffer);
        LOG.info("Executing task (size: {}) ... done", outputBuffer.size());
        return true;
    }

    private Repository createRepository() {
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        return repository;
    }

    private Collection<Statement> getInputData() throws LpException {
        this.chunk = manager.getChunk();
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
        if (deduplicateResults) {
            // Sparql construct does not return distinct results by default:
            // https://github.com/eclipse/rdf4j/issues/857
            result = QueryResults.distinctResults(result);
        }
        while (result.hasNext()) {
            outputBuffer.add(result.next());
        }
    }

    public boolean isFailed() {
        return failed;
    }

}
