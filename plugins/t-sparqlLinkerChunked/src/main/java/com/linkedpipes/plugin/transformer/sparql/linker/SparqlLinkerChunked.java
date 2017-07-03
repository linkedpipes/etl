package com.linkedpipes.plugin.transformer.sparql.linker;

import com.linkedpipes.etl.dataunit.core.rdf.ChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Linker is designed to solve problems, where links SPARQL construct
 * is execute on two big datasets, while it can be executed in isolation
 * on their chunks.
 *
 * For each reference chunk and each data chunk are put together,
 * over these data the given query is executed.
 *
 * Use the same vocabulary as SPARQL construct.
 */
public final class SparqlLinkerChunked implements Component,
        SequentialExecution {

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

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlConstructVocabulary.HAS_QUERY);
        }
        progressReport.start(dataRdf.size());
        final List<Statement> outputBuffer = new ArrayList<>(10000);
        // Load reference data.
        final List<Statement> reference = new ArrayList<>(10000);
        referenceRdf.execute((connection) -> {
            connection.export(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st)
                        throws RDFHandlerException {
                    reference.add(st);
                }
            }, referenceRdf.getReadGraph());
        });
        //
        boolean isAddToChunk = SparqlConstructVocabulary.ADD_TO_CHUNK.equals(
                configuration.getOutputMode());

        LOG.info("Output mode (add to chunk: {}) : {}", isAddToChunk,
                configuration.getOutputMode());

        for (ChunkedTriples.Chunk data : dataRdf) {
            LOG.info("processing ..");
            // Prepare repository and load data.
            final Repository repository =
                    new SailRepository(new MemoryStore());
            repository.initialize();
            LOG.info("\tloading ..");
            final Collection<Statement> statements = data.toCollection();
            Repositories.consume(repository, (connection) -> {
                connection.add(statements);
                connection.add(reference);
            });
            LOG.info("\tquerying ..");
            // Execute query and store result.
            Repositories.consume(repository, (connection) -> {
                final GraphQueryResult result =
                        connection.prepareGraphQuery(
                                configuration.getQuery()).evaluate();
                while (result.hasNext()) {
                    outputBuffer.add(result.next());
                }
            });
            if (isAddToChunk) {
                outputBuffer.addAll(statements);
            }
            outputRdf.submit(outputBuffer);
            LOG.info("\tcleanup ..");
            // Cleanup.
            outputBuffer.clear();
            repository.shutDown();
            progressReport.entryProcessed();
            LOG.info("\tdone ..");
        }
        progressReport.done();
    }


}
