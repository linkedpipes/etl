package com.linkedpipes.plugin.transformer.rdftofile;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.*;

import java.util.Collection;

/**
 * Add support for graph (context) renaming.
 */
public class RdfWriterContextRenamer implements RDFWriter {

    /**
     * Wrap for single statement used to change graph.
     */
    protected class StatementWrap implements Statement {

        protected Statement statement;

        @Override
        public Resource getSubject() {
            return statement.getSubject();
        }

        @Override
        public IRI getPredicate() {
            return statement.getPredicate();
        }

        @Override
        public Value getObject() {
            return statement.getObject();
        }

        @Override
        public Resource getContext() {
            return graph;
        }

    }

    /**
     * Underlying RDF writer.
     */
    private final RDFWriter writer;

    /**
     * Context used for graphs.
     */
    private final Resource graph;

    /**
     * Wrap used to change graph in statements.
     */
    private final StatementWrap statementWrap = new StatementWrap();

    public RdfWriterContextRenamer(RDFWriter writer, Resource graph) {
        this.writer = writer;
        this.graph = graph;
    }

    @Override
    public RDFFormat getRDFFormat() {
        return writer.getRDFFormat();
    }

    @Override
    public RDFWriter setWriterConfig(WriterConfig wc) {
        writer.setWriterConfig(wc);
        return this;
    }

    @Override
    public WriterConfig getWriterConfig() {
        return writer.getWriterConfig();
    }

    @Override
    public Collection<RioSetting<?>> getSupportedSettings() {
        return writer.getSupportedSettings();
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        writer.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        writer.endRDF();
    }

    @Override
    public void handleNamespace(String string, String string1)
            throws RDFHandlerException {
        writer.handleNamespace(string, string1);
    }

    @Override
    public void handleStatement(Statement stmnt) throws RDFHandlerException {
        // Replace graph = use our statement wrap.
        statementWrap.statement = stmnt;
        // Call original function.
        writer.handleStatement(statementWrap);
    }

    @Override
    public void handleComment(String string) throws RDFHandlerException {
        writer.handleComment(string);
    }

    @Override
    public <T> RDFWriter set(RioSetting<T> setting, T value) {
        writer.set(setting, value);
        return this;
    }

}
