package com.linkedpipes.plugin.transformer.filesToRdfGraph;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import java.util.ArrayList;
import java.util.List;

public class StatementInserter implements RDFHandler {

    private final int commintSize;

    private final WritableSingleGraphDataUnit dataUnit;

    private final List<Statement> statements;

    private IRI targetGraph;

    public StatementInserter(int commintSize,
            WritableSingleGraphDataUnit dataUnit) {
        this.commintSize = commintSize;
        this.dataUnit = dataUnit;
        this.statements = new ArrayList<>(commintSize);
        this.targetGraph = dataUnit.getWriteGraph();
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        // No operation here.
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        // Commit data in cache.
        try {
            dataUnit.execute((connection) -> {
                connection.begin();
                connection.add(statements, targetGraph);
                connection.commit();
            });
            statements.clear();
        } catch (LpException ex) {
            throw new RDFHandlerException(ex);
        }
    }

    @Override
    public void handleNamespace(String prefix, String uri)
            throws RDFHandlerException {
        // No operation here.
        try {
            dataUnit.execute((connection) -> {
                if (connection.getNamespace(prefix) == null) {
                    connection.setNamespace(prefix, uri);
                }
            });
        } catch (LpException ex) {
            throw new RDFHandlerException(ex);
        }
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        if (statements.size() >= commintSize) {
            try {
                dataUnit.execute((connection) -> {
                    connection.begin();
                    connection.add(statements, targetGraph);
                    connection.commit();
                });
                statements.clear();
            } catch (LpException ex) {
                throw new RDFHandlerException(ex);
            }
        }
        // We can enforce context here.
        statements.add(st);
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        // No operation here.
    }

}
