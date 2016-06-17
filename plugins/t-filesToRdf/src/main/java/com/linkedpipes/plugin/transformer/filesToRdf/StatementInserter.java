package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author Petr Škoda
 */
public class StatementInserter implements RDFHandler {

    private final int commintSize;

    private final WritableGraphListDataUnit dataUnit;

    private final List<Statement> statements;

    private URI targetGraph;

    public StatementInserter(int commintSize, WritableGraphListDataUnit dataUnit) {
        this.commintSize = commintSize;
        this.dataUnit = dataUnit;
        this.statements = new ArrayList<>(commintSize);
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
        } catch (SesameDataUnit.RepositoryActionFailed ex) {
            throw new RDFHandlerException(ex);
        }
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        // No operation here.
        try {
            dataUnit.execute((connection) -> {
                if (connection.getNamespace(prefix) == null) {
                    connection.setNamespace(prefix, uri);
                }
            });
        } catch (SesameDataUnit.RepositoryActionFailed ex) {
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
            } catch (SesameDataUnit.RepositoryActionFailed ex) {
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

    public URI getTargetGraph() {
        return targetGraph;
    }

    public void setTargetGraph(URI targetGraph) {
        this.targetGraph = targetGraph;
    }

}
