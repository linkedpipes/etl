package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.IDN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Takes CSV files on input. The CSV file rows are used as IRIs and mapped
 * to the given SPARQL as the ${VALUES} placeholder.
 *
 * Example query:
 * CONSTRUCT { ?obec ?p ?o } WHERE { ?obec ?p ?o ${VALUES} }
 * where the input CSV file contains column "obec".
 */
public final class SparqlEndpointChunked implements Component,
        SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlEndpointChunked.class);

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
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
        final SPARQLRepository repository;
        if (configuration.isUseTolerantRepository()) {
            repository = new TolerantSparqlRepository(getEndpoint());
        } else {
            repository = new SPARQLRepository(getEndpoint());
        }
        // Customize repository.
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (configuration.getTransferMimeType() != null) {
            headers.put("Accept", configuration.getTransferMimeType());
        }
        repository.setAdditionalHttpHeaders(headers);
        repository.setHttpClient(getHttpClient());
        return repository;
    }

    private String getEndpoint() {
        String[] tokens = configuration.getEndpoint().split("://", 2);
        return tokens[0] + "://" + IDN.toASCII(tokens[1]);
    }

    private CloseableHttpClient getHttpClient() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        if (configuration.isUseAuthentication()) {
            provider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(
                            configuration.getUsername(),
                            configuration.getPassword()));
        }
        return HttpClients.custom()
                .setDefaultCredentialsProvider(provider).build();
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
        try {
            tryToExecuteQuery(repository, query, buffer);
        } catch (LpException ex) {
            throw ex;
        } catch (Exception ex) {
            if (configuration.isSkipOnError()) {
                LOG.error("Failed to execute query.", ex);
            } else {
                throw exceptionFactory.failure("Failed to execute query.", ex);
            }
        }
    }

    protected void tryToExecuteQuery(Repository repository, String query,
            List<Statement> buffer) throws LpException {
        try (final RepositoryConnection connection =
                     repository.getConnection()) {
            final GraphQuery preparedQuery = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL, query);
            preparedQuery.setDataset(createDataset());
            RDFHandler handler = new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    buffer.add(st);
                }
            };
            if (configuration.isFixIncomingRdf()) {
                handler = new RdfEncodeHandler(handler);
            }
            preparedQuery.evaluate(handler);
        }
    }

}
