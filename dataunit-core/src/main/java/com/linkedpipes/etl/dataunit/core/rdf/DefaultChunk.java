package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

class DefaultChunk implements ChunkedTriples.Chunk {

    private final File file;

    public DefaultChunk(File file) {
        this.file = file;
    }

    @Override
    public Collection<Statement> toCollection() throws LpException {
        List<Statement> statements = new LinkedList<>();
        try (InputStream stream = new FileInputStream(this.file);
                Reader reader = new InputStreamReader(stream, "UTF-8")) {
            RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
            parser.setRDFHandler(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st)
                        throws RDFHandlerException {
                    statements.add(st);
                }
            });
            parser.parse(reader, "http://localhost/base/");
        } catch (Exception ex) {
            throw new LpException(
                    "Can't load chunk: {}", this.file.getName(), ex);
        }
        return statements;
    }

    @Override
    public String toString() {
        String name = this.file.getParentFile().getName()
                + "/" + this.file.getName();
        return "Chunk: " + name;
    }

}
