package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class QueryTaskExecutor implements TaskConsumer<QueryTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(QueryTaskExecutor.class);

    private final SparqlEndpointListConfiguration configuration;

    private final StatementsConsumer consumer;

    private final ProgressReport progressReport;

    private QueryTask task;

    public QueryTaskExecutor(
            SparqlEndpointListConfiguration configuration,
            StatementsConsumer consumer,
            ProgressReport progressReport) {
        this.configuration = configuration;
        this.consumer = consumer;
        this.progressReport = progressReport;
    }

    @Override
    public void accept(QueryTask task) throws LpException {
        this.task = task;
        Repository repository = createRepository();
        try {
            List<Statement> queryResult = executeQuery(repository);
            consumer.consume(queryResult);
        } finally {
            repository.shutDown();
            progressReport.entryProcessed();
        }
    }

    private SPARQLRepository createRepository() {
        SPARQLRepository repository = new SPARQLRepository(task.getEndpoint());
        setHeaders(repository);
        repository.initialize();
        return repository;
    }

    private void setHeaders(SPARQLRepository repository) {
        Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (task.getTransferMimeType() != null) {
            headers.put("Accept", task.getTransferMimeType());
        }
        repository.setAdditionalHttpHeaders(headers);
    }

    private List<Statement> executeQuery(Repository repository)
            throws LpException {
        try (RepositoryConnection connection = repository.getConnection()) {
            GraphQuery preparedQuery = createQuery(connection);
            List<Statement> statements = new LinkedList<>();
            RDFHandler handler = new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    statements.add(st);
                }
            };
            if (configuration.isFixIncomingRdf()) {
                handler = new RdfEncodeHandler(handler);
            }
            preparedQuery.evaluate(handler);
            return statements;
        }
    }

    private GraphQuery createQuery(RepositoryConnection connection) {
        GraphQuery query = connection.prepareGraphQuery(
                QueryLanguage.SPARQL, task.getQuery());
        setGraphsToQuery(query);
        query.setMaxExecutionTime(configuration.getExecutionTimeLimit());
        return query;
    }

    private void setGraphsToQuery(GraphQuery preparedQuery) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        SimpleDataset dataset = new SimpleDataset();
        for (String iri : task.getDefaultGraphs()) {
            dataset.addDefaultGraph(valueFactory.createIRI(iri));
        }
        preparedQuery.setDataset(dataset);
    }

}