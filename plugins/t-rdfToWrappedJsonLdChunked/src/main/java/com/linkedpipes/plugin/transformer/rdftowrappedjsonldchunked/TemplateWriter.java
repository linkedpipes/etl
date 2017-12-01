package com.linkedpipes.plugin.transformer.rdftowrappedjsonldchunked;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class TemplateWriter {

    public static final String PLACEHOLDER_ID = "{{ID}}";

    public static final String PLACEHOLDER_JSONLD = "{{JSONLD}}";

    public interface ChunkWriter {

        void write(Writer writer) throws IOException;

    }

    /**
     * Write a constant string.
     */
    private class StringChunk implements ChunkWriter {

        private final String value;

        public StringChunk(String value) {
            this.value = value;
        }

        public void write(Writer writer) throws IOException {
            writer.write(value);
        }

    }

    /**
     * Write ID from the owner class.
     */
    private class IdChunk implements ChunkWriter {

        @Override
        public void write(Writer writer) throws IOException {
            if (id != null) {
                writer.write(id);
            }
        }

    }

    /**
     * Chunk with RDF content.
     */
    private class RdfStatementsChunk implements ChunkWriter {

        @Override
        public void write(Writer writer) throws IOException {
            // TODO Set JSONLD format
            RDFWriter rdfWriter = Rio.createWriter(RDFFormat.JSONLD, writer);
            rdfWriter.startRDF();
            for (Statement statement : statements) {
                rdfWriter.handleStatement(statement);
            }
            rdfWriter.endRDF();
        }

    }

    private String id;

    private Collection<Statement> statements;

    private List<ChunkWriter> writers;

    public void createTemplate(String template) {
        writers = new ArrayList<>();
        while (true) {
            int startIndex = template.indexOf("{{");
            if (startIndex == -1) {
                writers.add(new StringChunk(template));
                break;
            }
            int endIndex = template.indexOf("}}");
            if (startIndex == -1) {
                writers.add(new StringChunk(template));
                break;
            }
            String beforeToken = template.substring(0, startIndex);
            String token = template.substring(startIndex, endIndex + 2);
            String afterToken = template.substring(endIndex + 2);
            //
            writers.add(new StringChunk(beforeToken));
            if (PLACEHOLDER_ID.equals(token)) {
                writers.add(new IdChunk());
            } else if (PLACEHOLDER_JSONLD.equals(token)) {
                writers.add(new RdfStatementsChunk());
            } else {
                writers.add(new StringChunk(token));
            }
            template = afterToken;
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatements(Collection<Statement> statements) {
        this.statements = statements;
    }

    public void writeToWriter(Writer writer) throws IOException {
        for (ChunkWriter chunkWriter : writers) {
            chunkWriter.write(writer);
        }
        writer.flush();
    }

}
