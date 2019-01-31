package com.linkedpipes.plugin.transformer.hdtToRdf;

import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import java.util.ArrayList;
import java.util.List;

class BufferedWriter implements RDFHandler {

    private final int commitSize;

    private final WritableSingleGraphDataUnit dataUnit;

    private final List<Statement> statements;

    public BufferedWriter(
            int commitSize, WritableSingleGraphDataUnit dataUnit) {
        this.commitSize = commitSize;
        this.dataUnit = dataUnit;
        this.statements = new ArrayList<>(commitSize);
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        // No operation here.
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        flushBuffer();
    }

    @Override
    public void handleNamespace(String prefix, String uri)
            throws RDFHandlerException {
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
        if (statements.size() >= commitSize) {
            flushBuffer();
        }
        statements.add(st);
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        // No operation here.
    }

    private void flushBuffer() {
        try {
            dataUnit.execute((connection) -> {
                connection.begin();
                connection.add(statements, dataUnit.getWriteGraph());
                connection.commit();
            });
            statements.clear();
        } catch (LpException ex) {
            throw new RDFHandlerException(ex);
        }
    }

}
