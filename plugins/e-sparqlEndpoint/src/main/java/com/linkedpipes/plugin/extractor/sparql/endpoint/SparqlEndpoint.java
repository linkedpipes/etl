package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.IDN;
import java.util.HashMap;
import java.util.Map;

public final class SparqlEndpoint implements Component, SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlEndpoint.class);

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public SparqlEndpointConfiguration configuration;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() throws LpException {
        if (configuration.getEndpoint() == null
                || configuration.getEndpoint().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlEndpointVocabulary.HAS_ENDPOINT);
        }
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlEndpointVocabulary.HAS_QUERY);
        }
        //
        final SPARQLRepository repository = createRepository();
        //
        try {
            repository.initialize();
        } catch (OpenRDFException ex) {
            throw exceptionFactory.failure("Can't connnect to endpoint.", ex);
        }
        repository.setHttpClient(getHttpClient());
        //
        try {
            queryRemote(repository);
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

    private SPARQLRepository createRepository() {
        TolerantSparqlRepository repository =
                new TolerantSparqlRepository(getEndpoint());
        if (configuration.isUseTolerantRepository()) {
            repository.fixMissingLanguageTag();
        }
        if (configuration.isHandleInvalid()) {
            repository.ignoreInvalidData();
        }
        // Set headers.
        Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (configuration.getTransferMimeType() != null) {
            headers.put("Accept", configuration.getTransferMimeType());
        }
        repository.setAdditionalHttpHeaders(headers);

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

    public void queryRemote(SPARQLRepository repository) throws LpException {
        final IRI graph = outputRdf.getWriteGraph();
        try (RepositoryConnection localConnection
                     = outputRdf.getRepository().getConnection()) {
            localConnection.begin();
            // We can't use Repositories.graphQuery (Repositories.get) here,
            // as Virtuoso fail with
            // 'No permission to execute procedure DB.DBA.SPARUL_RUN'
            // as sesame try to execute given action in a transaction.
            try (RepositoryConnection remoteConnection
                         = repository.getConnection()) {
                final GraphQuery preparedQuery
                        = remoteConnection.prepareGraphQuery(
                        QueryLanguage.SPARQL,
                        configuration.getQuery());
                // Construct dataset.
                final SimpleDataset dataset = new SimpleDataset();
                for (String iri : configuration.getDefaultGraphs()) {
                    dataset.addDefaultGraph(valueFactory.createIRI(iri));
                }
                preparedQuery.setDataset(dataset);
                RDFHandler handler = new AbstractRDFHandler() {
                    @Override
                    public void handleStatement(Statement st)
                            throws RDFHandlerException {
                        localConnection.add(st, graph);
                    }
                };
                if (configuration.isFixIncomingRdf()) {
                    handler = new RdfEncodeHandler(handler);
                }
                preparedQuery.evaluate(handler);
            }
            localConnection.commit();
        }
    }

}
