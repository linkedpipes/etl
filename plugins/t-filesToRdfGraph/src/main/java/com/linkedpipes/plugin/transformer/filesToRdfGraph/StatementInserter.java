package com.linkedpipes.plugin.transformer.filesToRdfGraph;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Petr Škoda
 */
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
        this.targetGraph = dataUnit.getGraph();
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
