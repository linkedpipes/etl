package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableChunkedStatements;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Takes CSV files on input. The CSV file rows are used as IRIs and mapped
 * to the given SPARQL as the ${VALUES} placeholder.
 *
 * Example query:
 *  CONSTRUCT { ?obec ?p ?o } WHERE { ?obec ?p ?o ${VALUES} }
 * where the input CSV file contains column "obec".
 *
 */
public final class SparqlEndpointChunked implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlEndpointChunked.class);

    @Component.InputPort(id = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(id = "OutputRdf")
    public WritableChunkedStatements outputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public SparqlEndpointChunkedConfiguration configuration;

    @Override
    public void execute() throws LpException {
        final Repository repository = createRepository();
        try {
            repository.initialize();
        } catch (OpenRDFException ex) {
            throw exceptionFactory.failure("Can't connect to endpoint.", ex);
        }

        final List<Statement> buffer = new ArrayList<>(50000);
        try {
            for (FilesDataUnit.Entry entry : inputFiles) {
                final ValuesSource valuesSource = new ValuesSource(
                        entry.toFile(), exceptionFactory,
                        configuration.getChunkSize());
                valuesSource.readSource((valuesClause) -> {
                    buffer.clear();
                    executeQuery(repository, valuesClause, buffer);
                    outputRdf.submit(buffer);
                });
            }
        } finally {
            try {
                repository.shutDown();
            } catch (RepositoryException ex) {
                LOG.error("Can't close repository.", ex);
            }
        }
    }

    protected Repository createRepository() {
        final SPARQLRepository repository = new SPARQLRepository(
                configuration.getEndpoint());
        // Customize repository.
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (configuration.getTransferMimeType() != null) {
            headers.put("Accept", configuration.getTransferMimeType());
        }
        repository.setAdditionalHttpHeaders(headers);
        return repository;
    }

    protected SimpleDataset createDataset() {
        final SimpleDataset dataset = new SimpleDataset();
        for (String iri : configuration.getDefaultGraphs()) {
            dataset.addDefaultGraph(
                    SimpleValueFactory.getInstance().createIRI(iri));
        }
        return dataset;
    }

    protected void executeQuery(Repository repository, String valueClause,
            List<Statement> buffer) throws LpException {
        final String query = configuration.getQuery().replace("${VALUES}",
                valueClause);
        LOG.debug("query:\n{}", query);
        try (final RepositoryConnection connection =
                     repository.getConnection()) {
            final GraphQuery preparedQuery = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL, query);
            preparedQuery.setDataset(createDataset());
            preparedQuery.evaluate(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st)
                        throws RDFHandlerException {
                    buffer.add(st);
                }
            });
        }
    }


}
