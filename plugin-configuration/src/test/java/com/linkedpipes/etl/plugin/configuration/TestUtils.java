package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestUtils {

    public static File fileFromResource(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource(fileName);
        if (url == null) {
            throw new RuntimeException(
                    "Required resource '" + fileName + "' is missing.");
        }
        return new File(url.getPath());
    }

    public static List<Statement> statementsFromResource(String fileName)
            throws IOException {
        File file = fileFromResource(fileName);
        try (InputStream stream = new FileInputStream(file)) {
            RDFFormat format = Rio.getParserFormatForFileName(fileName).get();
            return new ArrayList<>(Rio.parse(stream, "http://base", format));
        }
    }

    public static List<Statement> selectGraph(
            List<Statement> statements, String graph) {
        IRI iri = SimpleValueFactory.getInstance().createIRI(graph);
        return statements
                .stream()
                .filter(statement -> iri.equals(statement.getContext()))
                .collect(Collectors.toList());
    }

    public static void assertIsomorphic(
            List<Statement> actual, List<Statement> expected) {
        boolean isomorphic = Models.isomorphic(actual, expected);
        if (!isomorphic) {
            Set<Statement> actualSet = new HashSet<>(actual);
            Set<Statement> expectedSet = new HashSet<>(expected);
            System.out.println("Size expected: " + expectedSet.size()
                    + " actual: " + actualSet.size());
            System.out.println("Missing:");
            for (Statement statement : expectedSet) {
                if (actualSet.contains(statement)) {
                    continue;
                }
                System.out.println("- " + statement);
            }
            System.out.println("Extra:");
            for (Statement statement : actualSet) {
                if (expectedSet.contains(statement)) {
                    continue;
                }
                System.out.println("+ " + statement);
            }
        }
        Assertions.assertTrue(isomorphic);
    }

}
