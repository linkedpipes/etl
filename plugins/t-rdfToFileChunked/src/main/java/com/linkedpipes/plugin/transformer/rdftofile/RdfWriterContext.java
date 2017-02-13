package com.linkedpipes.plugin.transformer.rdftofile;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.*;

import java.util.Collection;

/**
 * Support cancel and progress report.
 */
public class RdfWriterContext implements RDFWriter {

    /**
     * Underlying RDF writer.
     */
    private final RDFWriter writer;

    public RdfWriterContext(RDFWriter writer) {
        this.writer = writer;
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
        // Ignore namespaces here.
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        writer.handleStatement(st);
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
