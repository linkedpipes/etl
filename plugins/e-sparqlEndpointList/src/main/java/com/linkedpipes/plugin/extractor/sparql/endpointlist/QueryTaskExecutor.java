package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.net.IDN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class QueryTaskExecutor implements TaskConsumer<QueryTask> {

    private final SparqlEndpointListConfiguration configuration;

    private final List<Statement> statements = new ArrayList<>();

    private final StatementsConsumer consumer;

    private final RDFHandler rdfHandler;

    public QueryTaskExecutor(
            SparqlEndpointListConfiguration configuration,
            StatementsConsumer consumer) {
        this.configuration = configuration;
        this.consumer = consumer;
        this.rdfHandler = createRdfHandler(configuration);
    }

    private RDFHandler createRdfHandler(
            SparqlEndpointListConfiguration configuration) {
        int commitSize = configuration.getCommitSize();
        RDFHandler handler;
        if (commitSize == 0) {
            handler = handlerCommitAtEnd();
        } else {
            handler = handlerCommitAfterSize(commitSize);
        }
        if (configuration.isFixIncomingRdf()) {
            handler = new RdfEncodeHandler(handler);
        }
        return handler;
    }

    private RDFHandler handlerCommitAtEnd() {
        return new AbstractRDFHandler() {
            @Override
            public void handleStatement(Statement st)
                    throws RDFHandlerException {
                statements.add(st);
            }

            @Override
            public void endRDF() throws RDFHandlerException {
                commitStatementsToConsumer();
            }
        };
    }

    private RDFHandler handlerCommitAfterSize(int size) {
        return new AbstractRDFHandler() {
            @Override
            public void handleStatement(Statement st)
                    throws RDFHandlerException {
                statements.add(st);
                if (statements.size() >= size) {
                    commitStatementsToConsumer();
                }
            }

            @Override
            public void endRDF() throws RDFHandlerException {
                commitStatementsToConsumer();
            }
        };
    }

    private void commitStatementsToConsumer() throws RDFHandlerException {
        try {
            consumer.consume(statements);
            statements.clear();
        } catch (LpException exception) {
            throw new RDFHandlerException("Can't save data.", exception);
        }
    }

    @Override
    public void accept(QueryTask task) throws LpException {
        validateTask(task);
        Repository repository = createRepository(task);
        try {
            executeQuery(task, repository);
        } finally {
            repository.shutDown();
        }
    }

    private void validateTask(QueryTask task) throws LpException {
        if (task.getEndpoint() == null) {
            throw new LpException("Missing endpoint for: {}", task.getIri());
        }
    }

    private SPARQLRepository createRepository(QueryTask task) {
        SPARQLRepository repository;
        String endpoint = getEndpoint(task);
        if (configuration.isUseTolerantRepository()) {
            repository = new TolerantSparqlRepository(endpoint);
        } else {
            repository = new SPARQLRepository(endpoint);
        }
        setHeaders(task, repository);
        repository.init();
        repository.setHttpClient(getHttpClient(task));
        return repository;
    }

    private String getEndpoint(QueryTask task) {
        String[] tokens = task.getEndpoint().split("://", 2);
        String[] url = tokens[1].split("/", 2);
        return tokens[0] + "://" + IDN.toASCII(url[0]) + "/" + url[1];
    }

    private void setHeaders(QueryTask task, SPARQLRepository repository) {
        Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (task.getTransferMimeType() != null) {
            headers.put("Accept", task.getTransferMimeType());
        }
        repository.setAdditionalHttpHeaders(headers);
    }

    private CloseableHttpClient getHttpClient(QueryTask task) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        if (task.isUseAuthentication()) {
            provider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(
                            task.getUsername(),
                            task.getPassword()));
        }
        return HttpClients.custom()
                .setDefaultCredentialsProvider(provider).build();
    }

    private void executeQuery(
            QueryTask task, Repository repository) {
        try (RepositoryConnection connection = repository.getConnection()) {
            GraphQuery preparedQuery = createQuery(task, connection);
            preparedQuery.evaluate(this.rdfHandler);
        }
    }

    private GraphQuery createQuery(
            QueryTask task, RepositoryConnection connection) {
        GraphQuery query = connection.prepareGraphQuery(
                QueryLanguage.SPARQL, task.getQuery());
        setGraphsToQuery(task, query);
        query.setMaxExecutionTime(configuration.getExecutionTimeLimit());
        return query;
    }

    private void setGraphsToQuery(
            QueryTask task, GraphQuery preparedQuery) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        SimpleDataset dataset = new SimpleDataset();
        for (String iri : task.getDefaultGraphs()) {
            dataset.addDefaultGraph(valueFactory.createIRI(iri));
        }
        preparedQuery.setDataset(dataset);
    }

}