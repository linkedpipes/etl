package com.linkedpipes.plugin.transformer.jsonldtofile;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.util.ArrayList;
import java.util.List;

public class StatementsCollector extends AbstractRDFHandler {

    private List<Statement> statements = new ArrayList<>();

    @Override
    public void startRDF() throws RDFHandlerException {
        super.startRDF();
        statements.clear();
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        super.handleStatement(st);
        this.statements.add(st);
    }

    public List<Statement> getStatements() {
        return statements;
    }

}
