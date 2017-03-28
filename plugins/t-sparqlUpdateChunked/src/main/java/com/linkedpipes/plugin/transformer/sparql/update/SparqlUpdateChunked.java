package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Chunked version of SparqlUpdate.
 */
public final class SparqlUpdateChunked implements Component,
        SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public ChunkedTriples inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.Configuration
    public SparqlUpdateConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlUpdateVocabulary.CONFIG_SPARQL);
        }
        // We always perform inserts.
        progressReport.start(inputRdf.size());
        List<Statement> outputBuffer = new ArrayList<>(10000);
        for (ChunkedTriples.Chunk chunk : inputRdf) {
            // Prepare repository and load data.
            final Repository repository = new SailRepository(new MemoryStore());
            repository.initialize();
            final Collection<Statement> statements = chunk.toCollection();
            Repositories.consume(repository, (connection) -> {
                connection.add(statements);
            });
            // Execute query.
            Repositories.consume(repository, (connection) -> {
                connection.prepareUpdate(
                        configuration.getQuery()).execute();
            });
            // Store data.
            Repositories.consume(repository, (connection) -> {
                connection.export(new AbstractRDFHandler() {
                    @Override
                    public void handleStatement(Statement st)
                            throws RDFHandlerException {
                        outputBuffer.add(st);
                    }
                });
            });
            outputRdf.submit(outputBuffer);
            // Cleanup.
            outputBuffer.clear();
            repository.shutDown();
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
