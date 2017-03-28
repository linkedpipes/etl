package com.linkedpipes.etl.rdf.utils.rdf4j;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.ContextStatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Use only for testing.
 */
public class Rdf4jTestUtils {

    private static final Logger LOG =
            LoggerFactory.getLogger(Rdf4jTestUtils.class);

    private Rdf4jTestUtils() {

    }

    public static boolean rdfEqual(String expectedResourceFile,
            Collection<Statement> actual) {
        File expectedFile = resourceToFile(expectedResourceFile);
        List<Statement> expected = loadRdfFile(expectedFile);
        return rdfEqual(expected, actual);
    }

    public static boolean rdfContains(String expectedResourceFile,
            Collection<Statement> subset) {
        File expectedFile = resourceToFile(expectedResourceFile);
        List<Statement> expected = loadRdfFile(expectedFile);
        return rdfContains(expected, subset);
    }

    private static File resourceToFile(String resourceFile) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(resourceFile);
        if (url == null) {
            throw new RuntimeException("Required resource '"
                    + resourceFile + "' is missing.");
        }
        return new File(url.getPath());
    }

    private static List<Statement> loadRdfFile(File file) {
        RDFFormat format = Rio.getParserFormatForFileName(file.getName()).get();
        if (format == null) {
            throw new RuntimeException("Invalid file extension.");
        }
        List<Statement> result = new ArrayList<>();
        try (InputStream stream = new FileInputStream(file)) {
            RDFParser parser = Rio.createParser(format);
            parser.setRDFHandler(new ContextStatementCollector(result,
                    SimpleValueFactory.getInstance()));
            parser.parse(stream, "http://localhost/base");
        } catch (IOException ex) {
            throw new RuntimeException("Can't load file.", ex);
        }
        return result;
    }

    public static boolean rdfEqual(Collection<Statement> expected,
            Collection<Statement> actual) {
        if (expected.size() != actual.size()) {
            return false;
        }
        return rdfContains(expected, actual);
    }

    public static boolean rdfContains(Collection<Statement> expected,
            Collection<Statement> subset) {
        Set<Statement> expectedSet = new HashSet();
        expectedSet.addAll(expected);
        boolean result = true;
        for (Statement statement : subset) {
            if (!expectedSet.contains(statement)) {
                LOG.info("Missing: {} {} {} {}",
                        statement.getSubject(),
                        statement.getPredicate(),
                        statement.getObject(),
                        statement.getContext());
                result = false;
            }
        }
        return result;
    }

}
