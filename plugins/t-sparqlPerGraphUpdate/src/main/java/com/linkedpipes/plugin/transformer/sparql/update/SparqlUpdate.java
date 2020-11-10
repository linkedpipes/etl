package com.linkedpipes.plugin.transformer.sparql.update;

import com.linkedpipes.etl.dataunit.core.rdf.GraphListDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableGraphListDataUnit;
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

import java.util.Collection;

public final class SparqlUpdate implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public GraphListDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableGraphListDataUnit outputRdf;

    @Component.Configuration
    public SparqlUpdateConfiguration configuration;

    @Override
    public void execute() throws LpException {
        copyStatementsAndProcessQuery();
    }

    private void copyStatementsAndProcessQuery() throws LpException {
        Collection<IRI> inputGraphs = inputRdf.getReadGraphs();
        for (final IRI inputGraph : inputGraphs) {
            inputRdf.execute((connection) -> {
                    IRI outputGraph = outputRdf.createGraph();
                    RepositoryResult<Statement> statement = connection
                        .getStatements(null, null, null, true, inputGraph);
                    addToOutput(statement, outputGraph);
                    executeUpdate(outputGraph);
                });
        }
    }

    private void addToOutput(RepositoryResult<Statement> statement, IRI outputGraph)
            throws LpException {
        outputRdf.execute((connection) -> {
            connection.add(statement, outputGraph);
        });
    }

    private void executeUpdate(IRI outputGraph) throws LpException {
        outputRdf.execute((connection) -> {
                Update update = connection.prepareUpdate(QueryLanguage.SPARQL,
                                                         configuration.getQuery());
                update.setDataset(createDataset(outputGraph));
                update.execute();
            });
    }

    private Dataset createDataset(IRI outputGraph) {
        SimpleDataset dataset = new SimpleDataset();
        dataset.addDefaultGraph(outputGraph);
        dataset.addDefaultRemoveGraph(outputGraph);
        dataset.setDefaultInsertGraph(outputGraph);
        return dataset;
    }
}
