package com.linkedpipes.etl.rdf.utils.rdf4j;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.ContextStatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Rdf4jUtils {

    private static final Logger LOG =
            LoggerFactory.getLogger(Rdf4jUtils.class);

    private Rdf4jUtils() {

    }

    public static void save(Collection<Statement> statements, File file)
            throws IOException, RdfUtilsException {
        RDFFormat format = getFormat(file.getName());
        try (OutputStream stream = new FileOutputStream(file)) {
            RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();
            for (Statement statement : statements) {
                writer.handleStatement(statement);
            }
            writer.endRDF();
        }
    }

    public static ClosableRdf4jSource loadAsSource(String resourceName)
            throws IOException {
        File file = resourceToFile(resourceName);
        RDFFormat format = getFormat(resourceName);
        ClosableRdf4jSource source = Rdf4jSource.createInMemory();
        Repository rdfRepository = source.getRepository();
        try (RepositoryConnection connection = rdfRepository.getConnection()) {
            connection.add(file, "http://localhost/default", format);
        } catch (RuntimeException | IOException ex) {
            source.close();
            throw ex;
        }
        return source;
    }

    public static List<Statement> loadAsStatements(String resourceName)
            throws IOException {
        File file = resourceToFile(resourceName);
        RDFFormat format = getFormat(resourceName);
        List<Statement> statements = new LinkedList<>();
        try (InputStream stream = new FileInputStream(file)) {
            RDFParser parser = Rio.createParser(format);
            parser.setRDFHandler(new AbstractRDFHandler() {

                @Override
                public void handleStatement(Statement st) {
                    statements.add(st);
                }

            });
            parser.parse(stream, "http://localhost/default");
        }
        return statements;
    }

    private static RDFFormat getFormat(String fileName) throws IOException {
        RDFFormat format = Rio.getParserFormatForFileName(fileName).get();
        if (format == null) {
            throw new IOException("Can't determine file format.");
        }
        return format;
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

    // TODO: Replace wih Models.isomorphic
    public static boolean rdfEqual(Collection<Statement> expected,
            Collection<Statement> actual) {
        boolean areEqual = diff(actual, expected, "-");
        areEqual &= diff(expected, actual, "+");
        return areEqual;
    }

    private static boolean diff(Collection<Statement> expected,
            Collection<Statement> subset, String prefix) {
        Set<Statement> expectedSet = new HashSet();
        expectedSet.addAll(expected);
        boolean result = true;
        for (Statement statement : subset) {
            if (!expectedSet.contains(statement)) {
                System.out.println(prefix + " <" +
                        statement.getSubject() + "> <" +
                        statement.getPredicate() + "> " +
                        statement.getObject().stringValue() + " : <" +
                        statement.getContext() + ">");
                result = false;
            }
        }
        return result;
    }

    // TODO: Replace wih Models.isSubset
    public static boolean rdfContains(Collection<Statement> expected,
            Collection<Statement> subset) {
        Set<Statement> expectedSet = new HashSet();
        expectedSet.addAll(expected);
        boolean result = true;
        for (Statement statement : subset) {
            if (!expectedSet.contains(statement)) {
                System.out.println("- <" +
                        statement.getSubject() + "> <" +
                        statement.getPredicate() + "> " +
                        statement.getObject().stringValue() + " : <" +
                        statement.getContext() + ">");
                result = false;
            }
        }
        return result;
    }

}
