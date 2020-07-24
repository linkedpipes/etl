package com.linkedpipes.plugin.transformer.sparql.construct;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public final class SparqlConstruct implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public SparqlConstructConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        checkConfiguration();
        executeQueryAndStoreResults();
    }

    private void checkConfiguration() throws LpException {
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.failure("Missing query.");
        }
    }

    private void executeQueryAndStoreResults() throws LpException {
        try {
            inputRdf.execute((connection) -> {
                GraphQueryResult result = executeQuery(connection);
                addResultToOutput(result);
            });
        } catch (Throwable t) {
            throw exceptionFactory.failure("Can't execute given query.", t);
        }
    }

    private GraphQueryResult executeQuery(RepositoryConnection connection) {
        final String queryAsString = configuration.getQuery();
        GraphQuery query = connection.prepareGraphQuery(
                QueryLanguage.SPARQL, queryAsString);
        query.setDataset(createDataset());
        return query.evaluate();
    }

    private Dataset createDataset() {
        SimpleDataset dataset = new SimpleDataset();
        dataset.addDefaultGraph(inputRdf.getReadGraph());
        return dataset;
    }

    private void addResultToOutput(GraphQueryResult result) throws LpException {
        outputRdf.execute((connection) -> {
            connection.add(
                    (Iterable<? extends Statement>) result,
                    outputRdf.getWriteGraph());
        });
    }

}
