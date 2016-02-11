package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;
import java.util.Collection;

import org.openrdf.model.Statement;
import org.openrdf.rio.*;

/**
 * Support cancel and progress report.
 *
 * @author Škoda Petr
 */
public class RdfWriterContext implements RDFWriter {

    private final ProgressReport progressReport;

    private final DataProcessingUnit.Context context;

    /**
     * Underlying RDF writer.
     */
    private final RDFWriter writer;

    public RdfWriterContext(RDFWriter writer, DataProcessingUnit.Context context, ProgressReport progressReport) {
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
            throw new RDFHandlerException(new DataProcessingUnit.ExecutionCancelled());
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
