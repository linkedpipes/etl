package com.linkedpipes.etl.library.rdf;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Optional;

/**
 * Support IO operations for statements.
 */
public class StatementsFile extends Statements {

    public StatementsFile(Collection<Statement> collection) {
        super(collection);
    }

    public void addAllIfExists(File file) throws IOException {
        if (file == null) {
            return;
        }
        Optional<RDFFormat> format =
                Rio.getParserFormatForFileName(file.getName());
        if (format.isEmpty() || !file.exists()) {
            return;
        }
        addAll(file, format.get());
    }

    public void addAll(File file, RDFFormat format) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            addAll(stream, format);
        }
    }

    public void addAll(InputStream stream, RDFFormat format)
            throws IOException {
        try {
            RDFParser parser = Rio.createParser(format);
            parser.setRDFHandler(new StatementCollector(collection));
            parser.parse(stream, "http://localhost/base");
        } catch (RuntimeException ex) {
            throw new IOException(ex);
        }
    }

    public void addAll(File file) throws IOException {
        Optional<RDFFormat> format =
                Rio.getParserFormatForFileName(file.getName());
        if (format.isEmpty()) {
            throw new IOException("Can't get format for: " + file.getName());
        }
        addAll(file, format.get());
    }

    public void writeToFile(File file, RDFFormat format) throws IOException {
        File directory = file.getParentFile();
        directory.mkdirs();
        try (OutputStream stream = new FileOutputStream(file)) {
            writeToStream(stream, format);
        }
    }

    public void writeToStream(OutputStream stream, RDFFormat format)
            throws IOException {
        try {
            Rio.write(collection, stream, format);
        } catch (RuntimeException ex) {
            throw new IOException("Can't write file.", ex);
        }
    }

    public void atomicWriteToFile(File file, RDFFormat format)
            throws IOException {
        File swp = new File(file.getParentFile(), file.getName() + ".swp");
        writeToFile(swp, format);
        Files.move(
                swp.toPath(), file.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }


}
