package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class TaskExecutor implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskExecutor.class);

    private Map<String, String> contextMap = MDC.getCopyOfContextMap();

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Task task;

    private final ErrorReportConsumer errorReportConsumer;

    private final TaskResultConsumer resultConsumer;

    private final ProgressReport progressReport;

    private final ExceptionFactory exceptionFactory;

    private final int executionTimeLimit;

    private SPARQLRepository repository;

    public TaskExecutor(
            Task task,
            ErrorReportConsumer errorReportConsumer,
            TaskResultConsumer resultConsumer,
            ProgressReport progressReport,
            ExceptionFactory exceptionFactory,
            int executionTimeLimit) {
        this.task = task;
        this.errorReportConsumer = errorReportConsumer;
        this.resultConsumer = resultConsumer;
        this.progressReport = progressReport;
        this.exceptionFactory = exceptionFactory;
        this.executionTimeLimit = executionTimeLimit;
    }

    @Override
    public void run() {
        MDC.setContextMap(contextMap);
        LOG.info("Executing: {} ...", task.getIri());
        try {
            createRepository();
            executeTask();
            LOG.info("Executing: {} ... done", task.getIri());
        } catch (Exception ex) {
            LOG.info("Executing: {} ... failed for runtime", task.getIri(), ex);
            errorReportConsumer.reportError(task, ex);
        } finally {
            progressReport.entryProcessed();
            try {
                if (repository != null) {
                    repository.shutDown();
                }
            } catch (Exception ex) {
                LOG.warn("Can't close repository.", ex);
            }
        }
    }

    private void createRepository() {
        repository = new SPARQLRepository(task.getEndpoint());
        setRepositoryHeaders();
        repository.initialize();
    }

    private void setRepositoryHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        String mimeType  = task.getTransferMimeType();
        if (mimeType != null) {
            headers.put("Accept", mimeType);
        }
        repository.setAdditionalHttpHeaders(headers);
    }

    private void executeTask() throws LpException {
        ValuesReader valuesReader = new ValuesReader(
                task.getFileWithChunks(),
                exceptionFactory,
                task.getChunkSize());
        valuesReader.readSource((values) -> {
            String query = prepareQuery(values);
            List<Statement> queryResult = executeQuery(query);
            resultConsumer.consume(queryResult);
        });
    }

    private String prepareQuery(String value) {
        return task.getQuery().replace("${VALUES}", value);
    }

    private List<Statement> executeQuery(String query)
            throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            GraphQuery preparedQuery = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL, query);
            setGraphsToQuery(preparedQuery);
            preparedQuery.setMaxExecutionTime(executionTimeLimit);
            try (GraphQueryResult result = preparedQuery.evaluate()) {
                return collectResults(result);
            } catch (RuntimeException ex) {
                LOG.error("Can't execute query: {}", task.getQuery());
                throw ex;
            }
        }
    }

    private void setGraphsToQuery(GraphQuery preparedQuery) {
        final SimpleDataset dataset = new SimpleDataset();
        for (String iri : task.getDefaultGraphs()) {
            dataset.addDefaultGraph(valueFactory.createIRI(iri));
        }
        preparedQuery.setDataset(dataset);
    }

    private List<Statement> collectResults(GraphQueryResult result) {
        List<Statement> statements = new LinkedList<>();
        while (result.hasNext()) {
            statements.add(result.next());
        }
        return statements;
    }

}
