package com.linkedpipes.plugin.check.rdfsinglegraph;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;

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

    /**
     * Extract all statements from given dataUnit into a model removing
     * graph.
     */
    private Model createModel(SingleGraphDataUnit dataUnit) throws LpException {
        Model model = new LinkedHashModel();
        IRI graph = dataUnit.getReadGraph();
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        dataUnit.execute((connection) -> {
            var statements = connection.getStatements(null, null, null, graph);
            for (Statement statement : statements) {
                var statementWithoutGraph = valueFactory.createStatement(
                        statement.getSubject(),
                        statement.getPredicate(),
                        statement.getObject());
                model.add(statementWithoutGraph);
            }
        });
        return model;
    }

}
