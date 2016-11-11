package com.linkedpipes.plugin.transformer.sparql.linker;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.ChunkedStatements;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableChunkedStatements;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.Repositories;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.AbstractRDFHandler;
import org.openrdf.sail.memory.MemoryStore;
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
public final class SparqlLinkerChunked implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlLinkerChunked.class);

    @Component.InputPort(id = "DataRdf")
    public ChunkedStatements dataRdf;

    @Component.InputPort(id = "ReferenceRdf")
    public SingleGraphDataUnit referenceRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(id = "OutputRdf")
    public WritableChunkedStatements outputRdf;

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
            }, referenceRdf.getGraph());
        });
        //
        for (ChunkedStatements.Chunk data : dataRdf) {
            LOG.info("processing ..");
            // Prepare repository and load data.
            final Repository repository =
                    new SailRepository(new MemoryStore());
            repository.initialize();
            LOG.info("\tloading ..");
            final Collection<Statement> statements = data.toStatements();
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
