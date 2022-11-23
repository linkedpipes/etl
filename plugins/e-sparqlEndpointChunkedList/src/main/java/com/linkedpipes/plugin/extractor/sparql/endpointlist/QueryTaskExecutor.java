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

    private final ProgressReport progressReport;

    private QueryTask task;

    private List<Statement> statements = new ArrayList<>();

    private final StatementsConsumer consumer;

    private final RDFHandler rdfHandler;

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private SPARQLRepository repository;

    private final Map<String, List<File>> inputFilesByName;

    public QueryTaskExecutor(
            SparqlEndpointChunkedListConfiguration configuration,
            StatementsConsumer consumer,
            ProgressReport progressReport,
            Map<String, List<File>> inputFilesByName) {
        this.configuration = configuration;
        this.progressReport = progressReport;
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
        validateTask(task);
        this.task = task;
        try {
            LOG.debug("Executing: {} ...", task.getIri());
            createRepository();
            executeTask();
            LOG.debug("Executing: {} ... done", task.getIri());
        } finally {
            progressReport.entryProcessed();
            repository.shutDown();
        }
    }

    private void validateTask(QueryTask task) throws LpException {
        if (task.getEndpoint() == null) {
            throw new LpException("Missing endpoint for: {}", task.getIri());
        }
    }

    private void createRepository() {
        if (configuration.isUseTolerantRepository()) {
            this.repository = new TolerantSparqlRepository(getEndpoint());
        } else {
            this.repository = new SPARQLRepository(getEndpoint());
        }
        setRepositoryHeaders(this.repository);
        this.repository.init();
        this.repository.setHttpClient(getHttpClient());
    }

    private String getEndpoint() {
        String[] tokens = task.getEndpoint().split("://", 2);
        String[] url = tokens[1].split("/", 2);
        return tokens[0] + "://" + IDN.toASCII(url[0]) + "/" + url[1];
    }

    private void setRepositoryHeaders(SPARQLRepository repository) {
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        String mimeType = this.task.getTransferMimeType();
        if (mimeType != null) {
            headers.put("Accept", mimeType);
        }
        repository.setAdditionalHttpHeaders(headers);
    }

    private CloseableHttpClient getHttpClient() {
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

    private void executeTask() throws LpException {
        CsvValuesReader valuesReader = new CsvValuesReader(
                task.getChunkSize(), task.getAsLiterals());
        valuesReader.setHandler((values) -> {
            String query = prepareQuery(values);
            executeQuery(query);
        });
        for (File file : getFilesForTask()) {
            valuesReader.readFile(file);
        }
    }

    private List<File> getFilesForTask() throws LpException {
        List<File> files = inputFilesByName.get(task.getFileName());
        if (files == null) {
            throw new LpException(
                    "No files find for task: {}", task.getIri());
        }
        return files;
    }

    private String prepareQuery(String value) {
        return task.getQuery().replace("${VALUES}", value);
    }

    private void executeQuery(String queryAsString) throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            GraphQuery query = createQuery(connection, queryAsString);
            query.evaluate(this.rdfHandler);
        }
    }

    private GraphQuery createQuery(
            RepositoryConnection connection, String queryAsString) {
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
