package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFWriter;

import java.util.Collection;
import java.util.Date;

class GetExecutionsHandler {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final ExecutionFacade executionFacade;

    public GetExecutionsHandler(ExecutionFacade executionFacade) {
        this.executionFacade = executionFacade;
    }

    public void handle(Long changedSince, RDFWriter writer) {
        Collection<Execution> executions;
        if (changedSince == null) {
            executions = executionFacade.getExecutions();
        } else {
            executions = executionFacade.getExecutions(new Date(changedSince));
        }

        for (Execution execution : executions) {
            this.writeStatements(execution.getOverviewStatements(), writer);
            this.writeStatements(execution.getMonitorStatements(), writer);
            this.writeStatements(execution.getPipelineStatements(), writer);
        }

        writer.handleStatement(valueFactory.createStatement(
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/metadata"),
                RDF.TYPE,
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/ontology/Metadata"),
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/metadata")));

        writer.handleStatement(valueFactory.createStatement(
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/metadata"),
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/ontology/serverTime"),
                valueFactory.createLiteral((new Date()).getTime()),
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/metadata")));
    }

    private void writeStatements(
            Collection<Statement> statements,
            RDFWriter writer) {
        for (Statement statement : statements) {
            writer.handleStatement(statement);
        }
    }


}
