package com.linkedpipes.plugin.transformer.sparql.construct;

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
import org.openrdf.sail.memory.MemoryStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Chunked version of SPARQL construct. Perform the construct operation
 * on chunks of RDF data.
 *
 * Use the same vocabulary as SPARQL construct.
 */
public final class SparqlConstructChunked implements Component.Sequential {

    @Component.InputPort(id = "InputRdf")
    public ChunkedStatements inputRdf;

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
        // We always perform inserts.
        progressReport.start(inputRdf.size());
        List<Statement> outputBuffer = new ArrayList<>(10000);
        for (ChunkedStatements.Chunk chunk: inputRdf) {
            // Prepare repository and load data.
            final Repository repository = new SailRepository(new MemoryStore());
            repository.initialize();
            final Collection<Statement> statements = chunk.toStatements();
            Repositories.consume(repository, (connection) -> {
                connection.add(statements);
            });
            // Execute query and store result.
            Repositories.consume(repository, (connection) -> {
                final GraphQueryResult result = connection.prepareGraphQuery(
                        configuration.getQuery()).evaluate();
                while (result.hasNext()) {
                    outputBuffer.add(result.next());
                }
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
