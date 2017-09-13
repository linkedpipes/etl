package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
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
import java.util.*;

class QueryTaskExecutor implements TaskConsumer<QueryTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(QueryTaskExecutor.class);

    private final SparqlEndpointChunkedListConfiguration configuration;

    private final ProgressReport progressReport;

    private QueryTask task;

    private List<Statement> statements = new ArrayList<>();

    private final StatementsConsumer consumer;

    private final RDFHandler rdfHandler;

    private final ExceptionFactory exceptionFactory;

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private SPARQLRepository repository;

    private final Map<String, List<File>> inputFilesByName;

    public QueryTaskExecutor(
            SparqlEndpointChunkedListConfiguration configuration,
            StatementsConsumer consumer,
            ProgressReport progressReport,
            ExceptionFactory exceptionFactory,
            Map<String, List<File>> inputFilesByName) {
        this.configuration = configuration;
        this.progressReport = progressReport;
        this.consumer = consumer;
        this.rdfHandler = createRdfHandler(configuration);
        this.exceptionFactory = exceptionFactory;
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

    private void createRepository() {
        this.repository = new SPARQLRepository(this.task.getEndpoint());
        setRepositoryHeaders(this.repository);
        this.repository.initialize();
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

    private void executeTask() throws LpException {
        CsvValuesReader valuesReader = new CsvValuesReader(
                exceptionFactory, task.getChunkSize());
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
            throw exceptionFactory.failure(
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
