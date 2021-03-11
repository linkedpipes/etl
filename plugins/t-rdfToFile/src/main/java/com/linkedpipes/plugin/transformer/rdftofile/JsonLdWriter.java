package com.linkedpipes.plugin.transformer.rdftofile;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFWriter;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.IOException;
import java.io.Writer;

/**
 * Customized writer, to force some options to JsonLdProcessor.
 * See {@link #createOptions()} for more details.
 */
public class JsonLdWriter extends AbstractRDFWriter {

    private final Model model = new LinkedHashModel();

    private final StatementCollector statementCollector =
            new StatementCollector(model);

    private final Writer writer;

    public JsonLdWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        statementCollector.handleStatement(st);
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        super.startRDF();
        clear();
    }

    public void clear() {
        statementCollector.clear();
        model.clear();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        try {
            write(model, writer);
        } catch (IOException ex) {
            throw new RDFHandlerException("Can't write content.", ex);
        }
        clear();
    }

    protected void write(Model model, Writer writer)
            throws IOException {
        JSONLDInternalRDFParser serializer = new JSONLDInternalRDFParser();
        JsonLdOptions options = createOptions();
        Object outputObject = JsonLdProcessor.fromRDF(
                model, options, serializer);
        JsonUtils.writePrettyPrint(writer, outputObject);
    }

    protected JsonLdOptions createOptions() {
        JsonLdOptions options = new JsonLdOptions();
        options.setUseNativeTypes(true);
        return options;
    }

    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.JSONLD;
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        // Do nothing.
    }
}
