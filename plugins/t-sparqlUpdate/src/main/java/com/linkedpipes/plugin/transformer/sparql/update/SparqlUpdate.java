package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryResult;

public final class SparqlUpdate implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public SparqlUpdateConfiguration configuration;

    @Override
    public void execute() throws LpException {
        copyStatements();
        executeUpdate();
    }

    private void copyStatements() throws LpException {
        IRI inputGraph = inputRdf.getReadGraph();
        inputRdf.execute((connection) -> {
            RepositoryResult<Statement> statement = connection
                    .getStatements(null, null, null, true, inputGraph);
            addToOutput(statement);
        });
    }

    private void addToOutput(RepositoryResult<Statement> statement)
            throws LpException {
        IRI outputGraph = outputRdf.getWriteGraph();
        outputRdf.execute((connection) -> {
            connection.add(statement, outputGraph);
        });
    }

    private void executeUpdate() throws LpException {
        outputRdf.execute((connection) -> {
            Update update = connection.prepareUpdate(QueryLanguage.SPARQL,
                    configuration.getQuery());
            update.setDataset(createDataset());
            update.execute();
        });
    }

    private Dataset createDataset() {
        IRI outputGraph = outputRdf.getWriteGraph();
        SimpleDataset dataset = new SimpleDataset();
        dataset.addDefaultGraph(outputGraph);
        dataset.addDefaultRemoveGraph(outputGraph);
        dataset.setDefaultInsertGraph(outputGraph);
        return dataset;
    }
}
