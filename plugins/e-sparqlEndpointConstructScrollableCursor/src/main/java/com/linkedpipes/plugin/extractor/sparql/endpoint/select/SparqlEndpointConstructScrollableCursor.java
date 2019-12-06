package com.linkedpipes.plugin.extractor.sparql.endpoint.select;

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
import java.util.List;

/**
 * Use scrollable cursors to execute SPARQL construct.
 */
public final class SparqlEndpointConstructScrollableCursor
        implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(
            SparqlEndpointConstructScrollableCursor.class);

    @Component.InputPort(iri = "OutputFiles")
    public WritableChunkedTriples outputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Configuration
    public SparqlEndpointConstructScrollableCursorConfiguration configuration;

    @Override
    public void execute() throws LpException {
        SPARQLRepository repository;
        if (configuration.isUseTolerantRepository()) {
            repository = new TolerantSparqlRepository(getEndpoint());
        } else {
            repository = new SPARQLRepository(getEndpoint());
        }
        repository.initialize();
        repository.setHttpClient(getHttpClient());
        //
        LOG.info("Used query: {}", prepareQuery(0));
        try {
            int offset = 0;
            final List<Statement> buffer = new ArrayList<>(100000);
            while (true) {
                LOG.info("offset: {}", offset);
                executeQuery(repository, offset, buffer);
                if (buffer.isEmpty()) {
                    break;
                } else {
                    outputRdf.submit(buffer);
                }
                offset += configuration.getPageSize();
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

    private String getEndpoint() {
        String[] tokens = configuration.getEndpoint().split("://", 2);
        String[] url = tokens[1].split("/", 2);
        return tokens[0] + "://" + IDN.toASCII(url[0]) + "/" + url[1];
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

    /**
     * @param repository
     * @param offset
     */
    protected void executeQuery(Repository repository, int offset,
            List<Statement> buffer) throws LpException {
        try (final RepositoryConnection connection =
                     repository.getConnection()) {
            //
            final GraphQuery query = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL, prepareQuery(offset));
            //
            final SimpleDataset dataset = new SimpleDataset();
            for (String iri : configuration.getDefaultGraphs()) {
                if (!iri.isEmpty()) {
                    dataset.addDefaultGraph(
                            SimpleValueFactory.getInstance().createIRI(iri));
                }
            }
            query.setDataset(dataset);
            buffer.clear();

            RDFHandler handler = new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    buffer.add(st);
                }
            };
            if (configuration.isFixIncomingRdf()) {
                handler = new RdfEncodeHandler(handler);
            }
            query.evaluate(handler);
        }
    }

    protected String prepareQuery(int offset) {
        return configuration.getPrefixes() + "\n CONSTRUCT {\n" +
                configuration.getOuterConstruct() + "\n } WHERE { {" +
                configuration.getInnerSelect() +
                "\n} }" +
                "\nLIMIT " + Integer.toString(configuration.getPageSize()) +
                "\nOFFSET " + Integer.toString(offset);
    }

}
