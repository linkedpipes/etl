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
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.IDN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class QueryTaskExecutor implements TaskConsumer<QueryTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(QueryTaskExecutor.class);

    private final SparqlEndpointChunkedListConfiguration configuration;

    private final List<Statement> statements = new ArrayList<>();

    private final StatementsConsumer consumer;

    private final RDFHandler rdfHandler;

    private final  ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Map<String, List<File>> inputFilesByName;

    public QueryTaskExecutor(
            SparqlEndpointChunkedListConfiguration configuration,
            StatementsConsumer consumer,
            Map<String, List<File>> inputFilesByName) {
        this.configuration = configuration;
        this.consumer = consumer;
        this.rdfHandler = createRdfHandler(configuration);
        this.inputFilesByName = inputFilesByName;
    }

    private RDFHandler createRdfHandler(
            SparqlEndpointChunkedListConfiguration configuration) {
        RDFHandler handler = handlerCommitAtEnd();
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
        SPARQLRepository repository = null;
        validateTask(task);
        try {
            repository = createRepository(task);
            executeTask(task, repository);
        } finally {
            if (repository != null) {
                repository.shutDown();
            }
        }
    }

    private void validateTask(QueryTask task) throws LpException {
        if (task.getEndpoint() == null) {
            throw new LpException("Missing endpoint for: {}", task.getIri());
        }
    }

    private SPARQLRepository createRepository(QueryTask task) {
        SPARQLRepository result;
        String endpoint = getEndpoint(task);
        if (configuration.isUseTolerantRepository()) {
            result = new TolerantSparqlRepository(endpoint);
        } else {
            result = new SPARQLRepository(endpoint);
        }
        setRepositoryHeaders(task, result);
        result.init();
        result.setHttpClient(getHttpClient(task));
        return result;
    }

    private String getEndpoint(QueryTask task) {
        String[] tokens = task.getEndpoint().split("://", 2);
        String[] url = tokens[1].split("/", 2);
        return tokens[0] + "://" + IDN.toASCII(url[0]) + "/" + url[1];
    }

    private void setRepositoryHeaders(
            QueryTask task, SPARQLRepository repository) {
        final Map<String, String> headers = new HashMap<>(
                repository.getAdditionalHttpHeaders());
        String mimeType = task.getTransferMimeType();
        if (mimeType != null) {
            headers.put("Accept", mimeType);
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

    private void executeTask(QueryTask task, SPARQLRepository repository)
            throws LpException {
        CsvValuesReader valuesReader = new CsvValuesReader(
                task.getChunkSize(), task.getAsLiterals());
        valuesReader.setHandler((values) -> {
            String query = prepareQuery(task, values);
            executeQuery(task, repository, query);
        });
        for (File file : getFilesForTask(task)) {
            valuesReader.readFile(file);
        }
    }

    private List<File> getFilesForTask(QueryTask task) throws LpException {
        List<File> files = inputFilesByName.get(task.getFileName());
        if (files == null) {
            throw new LpException(
                    "No files find for task: {}", task.getIri());
        }
        return files;
    }

    private String prepareQuery(QueryTask task, String value) {
        return task.getQuery().replace("${VALUES}", value);
    }

    private void executeQuery(
            QueryTask task, SPARQLRepository repository, String queryAsString) {
        try (RepositoryConnection connection = repository.getConnection()) {
            GraphQuery query = createQuery(task, connection, queryAsString);
            query.evaluate(this.rdfHandler);
        }
    }

    private GraphQuery createQuery(
            QueryTask task, RepositoryConnection connection,
            String queryAsString) {
        GraphQuery query = connection.prepareGraphQuery(
                QueryLanguage.SPARQL, queryAsString);
        setGraphsToQuery(task, query);
        query.setMaxExecutionTime(configuration.getExecutionTimeLimit());
        return query;
    }

    private void setGraphsToQuery(QueryTask task, GraphQuery preparedQuery) {
        SimpleDataset dataset = new SimpleDataset();
        for (String iri : task.getDefaultGraphs()) {
            dataset.addDefaultGraph(valueFactory.createIRI(iri));
        }
        preparedQuery.setDataset(dataset);
    }


}
