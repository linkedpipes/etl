package com.linkedpipes.plugin.transformer.rdfToHdt;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import org.eclipse.rdf4j.common.io.IndentingWriter;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.triples.TripleString;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

public class HdtTripleIterator extends TurtleWriter implements Iterator<TripleString>, AutoCloseable {

    private RepositoryConnection connection;

    private RepositoryResult<Statement> repositoryResult;

    private StringWriter writer = new StringWriter();

    public HdtTripleIterator(SingleGraphDataUnit inputRdf) {
        super((Writer) null);
        // Initialize internal writer, we need to call
        // startRDF else conversion fail.
        WriterConfig config = new WriterConfig();
        config.set(BasicWriterSettings.PRETTY_PRINT, false);
        super.setWriterConfig(config);
        super.startRDF();
        //
        super.writer = new IndentingWriter(writer);
        this.connection = inputRdf.getRepository().getConnection();
        this.repositoryResult = connection.getStatements(
                null, null, null, inputRdf.getReadGraph());
    }

    @Override
    public boolean hasNext() {
        return repositoryResult.hasNext();
    }

    @Override
    public TripleString next() {
        Statement st = repositoryResult.next();
        if (st == null) {
            return null;
        }
        TripleString result = new TripleString();
        try {
            writer.getBuffer().setLength(0);
            writeResource(st.getSubject(), false);
            writer.getBuffer().append(" ");
            writeValue(st.getPredicate(), false);
            writer.getBuffer().append(" ");
            writeValue(st.getObject(), false);
            //
            String line = extractBufferContent();
            result.read(line);
        } catch (IOException | ParserException ex) {
            throw new RuntimeException(
                    "Can't convert a statement. {}" + st.toString(),
                    ex);
        }
        return result;
    }

    private String extractBufferContent() {
        String str = writer.toString();
        writer.getBuffer().setLength(0);
        return str;
    }

    @Override
    public void close() {
        if (connection != null) {
            connection.close();
        }
    }
}
