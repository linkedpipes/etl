package com.linkedpipes.plugin.check.rdfsinglegraph;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.repository.RepositoryResult;

public class CheckRdfSingleGraph implements Component, SequentialExecution {

    @Component.InputPort(iri = "Expected")
    public SingleGraphDataUnit expected;

    @Component.InputPort(iri = "Actual")
    public SingleGraphDataUnit actual;

    @Override
    public void execute() throws LpException {
        Model expectedModel = createModel(expected);
        Model actualModel = createModel(actual);
        if (!Models.isomorphic(expectedModel, actualModel)) {
            throw new LpException(
                    "Expected and Actual inputs are not isomorphic.");
        }
    }

    private Model createModel(SingleGraphDataUnit dataUnit) throws LpException {
        Model model = new LinkedHashModel();
        IRI graph = dataUnit.getReadGraph();
        dataUnit.execute((connection) -> {
            RepositoryResult<Statement> statements =
                    connection.getStatements(null, null, null, graph);
            RemoveGraphIterator graphLessStatements =
                    new RemoveGraphIterator(statements);
            Iterations.addAll(graphLessStatements, model);
        });
        return model;
    }

}
