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

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * All tasks must have the same repository.
 */
class TaskExecutor implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskExecutor.class);

    private Map<String, String> contextMap = MDC.getCopyOfContextMap();

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final List<Task> tasks;

    private final ErrorReportConsumer errorReportConsumer;

    private final TaskResultConsumer resultConsumer;

    private final ProgressReport progressReport;

    private final ExceptionFactory exceptionFactory;

    private final int executionTimeLimit;

    private SPARQLRepository repository;

    private final Map<String, List<File>> inputFilesByName;

    public TaskExecutor(
            List<Task> tasks,
            ErrorReportConsumer errorReportConsumer,
            TaskResultConsumer resultConsumer,
            ProgressReport progressReport,
            ExceptionFactory exceptionFactory,
            int executionTimeLimit,
            Map<String, List<File>> inputFilesByName) {
        this.tasks = tasks;
        this.errorReportConsumer = errorReportConsumer;
        this.resultConsumer = resultConsumer;
        this.progressReport = progressReport;
        this.exceptionFactory = exceptionFactory;
        this.executionTimeLimit = executionTimeLimit;
        this.inputFilesByName = inputFilesByName;
    }

    @Override
    public void run() {
        MDC.setContextMap(contextMap);
        if (tasks.isEmpty()) {
            LOG.warn("No tasks to execute.");
            return;
        }
        for (Task task : tasks) {
            prepareRepositoryAndExecuteTask(task);
        }
    }

    private void prepareRepositoryAndExecuteTask(Task task) {
        try {
            createRepository(task);
            LOG.info("Executing: {} ...", task.getIri());
            executeTask(task);
            LOG.info("Executing: {} ... done", task.getIri());
        } catch (Exception ex) {
            LOG.info("Executing: {} ... failed ", task.getIri(), ex);
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

    private void createRepository(Task task) {
        repository = new SPARQLRepository(task.getEndpoint());
        setRepositoryHeaders(task);
        repository.initialize();
    }

    private void setRepositoryHeaders(Task task) {
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        String mimeType = task.getTransferMimeType();
        if (mimeType != null) {
            headers.put("Accept", mimeType);
        }
        repository.setAdditionalHttpHeaders(headers);
    }

    private void executeTask(Task task) throws LpException {
        for (File file : getFilesForTask(task)) {
            readValueFromFileAndExecuteTask(task, file);
        }
    }
    private void readValueFromFileAndExecuteTask(Task task,
            File fileWithValues) throws LpException {
        ValuesReader valuesReader = new ValuesReader(
                fileWithValues,
                exceptionFactory,
                task.getChunkSize());
        valuesReader.readSource((values) -> {
            String query = prepareQuery(task, values);
            List<Statement> queryResult = executeQuery(task, query);
            resultConsumer.consume(queryResult);
        });
    }

    private List<File> getFilesForTask(Task task) throws LpException {
        List<File> files = inputFilesByName.get(task.getFileName());
        if (files == null) {
            throw exceptionFactory.failure("No files find for task: {}",
                    task.getIri());
        }
        return files;
    }

    private String prepareQuery(Task task, String value) {
        return task.getQuery().replace("${VALUES}", value);
    }

    private List<Statement> executeQuery(Task task, String query)
            throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            GraphQuery preparedQuery = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL, query);
            setGraphsToQuery(task, preparedQuery);
            preparedQuery.setMaxExecutionTime(executionTimeLimit);
            try (GraphQueryResult result = preparedQuery.evaluate()) {
                return collectResults(result);
            } catch (RuntimeException ex) {
                LOG.error("Can't execute query: {}", query);
                throw ex;
            }
        }
    }

    private void setGraphsToQuery(Task task, GraphQuery preparedQuery) {
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
