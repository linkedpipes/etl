package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
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
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
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

    private Map<String,String> contextMap = MDC.getCopyOfContextMap();

    private ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Task task;

    private final ErrorReportConsumer errorReportConsumer;

    private final TaskResultConsumer resultConsumer;

    private final ProgressReport progressReport;

    private final int executionTimeLimit;

    private final boolean fixIncomingRdf;

    public TaskExecutor(
            Task task,
            ErrorReportConsumer errorReportConsumer,
            TaskResultConsumer resultConsumer,
            ProgressReport progressReport,
            int executionTimeLimit,
            boolean fixIncomingRdf) {
        this.task = task;
        this.errorReportConsumer = errorReportConsumer;
        this.resultConsumer = resultConsumer;
        this.progressReport = progressReport;
        this.executionTimeLimit = executionTimeLimit;
        this.fixIncomingRdf = fixIncomingRdf;
    }

    @Override
    public void run() {
        MDC.setContextMap(contextMap);
        LOG.info("Executing: {} ...", task.getIri());
        SPARQLRepository repository = createRepository(
                task.getEndpoint(), task.getTransferMimeType());
        repository.initialize();
        try {
            List<Statement> queryResult = executeQuery(repository);
            resultConsumer.consume(queryResult);
        } catch (Exception ex) {
            errorReportConsumer.reportError(task, ex);
        }
        repository.shutDown();
        progressReport.entryProcessed();
        LOG.info("Executing: {} ... done", task.getIri());
    }

    private static SPARQLRepository createRepository(
            String endpoint, String mimeType) {
        SPARQLRepository repository = new SPARQLRepository(endpoint);
        final Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (mimeType != null) {
            headers.put("Accept", mimeType);
        }
        repository.setAdditionalHttpHeaders(headers);
        return repository;
    }

    private List<Statement> executeQuery(Repository repository)
            throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            GraphQuery preparedQuery = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL, task.getQuery());
            setGraphsToQuery(preparedQuery);
            preparedQuery.setMaxExecutionTime(executionTimeLimit);

            List<Statement> statements = new LinkedList<>();
            RDFHandler handler = new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    statements.add(st);
                }
            };
            if (fixIncomingRdf) {
                handler = new RdfEncodeHandler(handler);
            }
            preparedQuery.evaluate(handler);
            return statements;
        }
    }

    private void setGraphsToQuery(GraphQuery preparedQuery) {
        final SimpleDataset dataset = new SimpleDataset();
        for (String iri : task.getDefaultGraphs()) {
            dataset.addDefaultGraph(valueFactory.createIRI(iri));
        }
        preparedQuery.setDataset(dataset);
    }

}
