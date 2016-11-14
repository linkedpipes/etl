package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableChunkedStatements;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.SimpleDataset;
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
 * Chunked version of SparqlEndpoint extractor. Takes a set of construct
 * queries on input. Execute each construct and put output to separated
 * chunk.
 */
public final class SparqlEndpointChunked implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlEndpointChunked.class);

    @Component.InputPort(id = "OutputRdf")
    public WritableChunkedStatements outputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public SparqlEndpointChunkedConfiguration configuration;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private List<Statement> buffer = new ArrayList<>(10000);

    @Override
    public void execute() throws LpException {
        if (configuration.getEndpoint() == null
                || configuration.getEndpoint().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlEndpointChunkedVocabulary.HAS_ENDPOINT);
        }
        //
        final SPARQLRepository repository
                = new SPARQLRepository(configuration.getEndpoint());
        // Customize repository.
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (configuration.getTransferMimeType() != null) {
            headers.put("Accept", configuration.getTransferMimeType());
        }
        repository.setAdditionalHttpHeaders(headers);
        //
        try {
            repository.initialize();
        } catch (OpenRDFException ex) {
            throw exceptionFactory.failure("Can't connect to endpoint.", ex);
        }
        //
        try {
            for (SparqlEndpointChunkedConfiguration.Query query
                    : configuration.getQueries()) {
                queryRemote(repository, query.getQuery(),
                        query.getDefaultGraphs());
            }
        } catch (Throwable t) {
            throw exceptionFactory.failure("Can't query remote SPARQL.", t);
        } finally {
            try {
                repository.shutDown();
            } catch (RepositoryException ex) {
                LOG.error("Can't close repository.", ex);
            }
        }
    }

    public void queryRemote(SPARQLRepository repository, String query,
            List<String> defaultGraphs) throws LpException {
        try (RepositoryConnection remoteConnection
                     = repository.getConnection()) {
            final GraphQuery preparedQuery
                    = remoteConnection.prepareGraphQuery(
                    QueryLanguage.SPARQL, query);
            // Construct dataset.
            final SimpleDataset dataset = new SimpleDataset();
            for (String iri : defaultGraphs) {
                dataset.addDefaultGraph(valueFactory.createIRI(iri));
            }
            preparedQuery.setDataset(dataset);
            buffer.clear();
            preparedQuery.evaluate(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st)
                        throws RDFHandlerException {
                    buffer.add(st);
                }
            });
            outputRdf.submit(buffer);
        }
    }

}
