package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Chunked version of SPARQL construct. Perform the construct operation
 * on chunks of RDF data.
 *
 * TODO: Use the same vocabulary as SPARQL construct.
 */
public final class SparqlConstructChunked implements Component,
        SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public ChunkedTriples inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.Configuration
    public SparqlConstructConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    private ExecutorManager executorManager;

    private List<SparqlConstructExecutor> executors = new LinkedList<>();

    @Override
    public void execute() throws LpException {
        checkConfiguration();
        createExecutors();
        progressReport.start(inputRdf.size());

        ExecutorService executor = Executors.newFixedThreadPool(
                configuration.getNumberOfThreads());
        for (SparqlConstructExecutor constructExecutor : executors) {
            executor.submit(constructExecutor);
        }
        executor.shutdown();

        while (true) {
            try {
                if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }

        for (SparqlConstructExecutor constructExecutor : executors) {
            if (constructExecutor.isFailed()) {
                throw exceptionFactory.failure("One construct failed.");
            }
        }
        progressReport.done();
    }

    private void createExecutors() {
        executorManager = new ExecutorManager(
                inputRdf, outputRdf, progressReport);
        for (int i = 0; i < configuration.getNumberOfThreads(); ++i) {
            SparqlConstructExecutor constructExecutor =
                    new SparqlConstructExecutor(
                            executorManager, configuration.getQuery());
            executors.add(constructExecutor);
        }
    }

    private void checkConfiguration() throws LpException {
        String query = configuration.getQuery();
        if (query == null || query.isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlConstructVocabulary.HAS_QUERY);
        }
    }

}
