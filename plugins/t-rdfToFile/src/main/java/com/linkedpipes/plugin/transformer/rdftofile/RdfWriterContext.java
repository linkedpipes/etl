package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.dpu.api.service.ProgressReport;
import java.util.Collection;

import org.openrdf.model.Statement;
import org.openrdf.rio.*;
import com.linkedpipes.etl.dpu.api.Component;

/**
 * Support cancel and progress report.
 *
 * @author Škoda Petr
 */
public class RdfWriterContext implements RDFWriter {

    private final ProgressReport progressReport;

    private final Component.Context context;

    /**
     * Underlying RDF writer.
     */
    private final RDFWriter writer;

    public RdfWriterContext(RDFWriter writer, Component.Context context, ProgressReport progressReport) {
        this.writer = writer;
        this.context = context;
        this.progressReport = progressReport;
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
    public void handleNamespace(String string, String string1) throws RDFHandlerException {
        writer.handleNamespace(string, string1);
    }

    @Override
    public void handleStatement(Statement stmnt) throws RDFHandlerException {
        progressReport.entryProcessed();
        writer.handleStatement(stmnt);
        if (context.canceled()) {
            throw new RDFHandlerException(new Component.ExecutionCancelled());
        }
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
