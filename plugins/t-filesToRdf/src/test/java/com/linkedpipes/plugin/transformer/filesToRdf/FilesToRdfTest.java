package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.test.TestEnvironment;
import com.linkedpipes.etl.test.TestUtils;
import com.linkedpipes.etl.test.dataunit.TestGraphListDataUnit;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FilesToRdfTest {

    private static final String JSON_LD = """
            {
              "@id": "http://localhost/%s",
              "http://localhost/title": "%s",
              "http://localhost/part": { "http://localhost/label": "part" }
            }
            """;

    /**
     * Each JSON-LD file must end up, complete, in its own graph. The component
     * writes its buffer in endRDF; the RDF4J 5.0.1 JSON-LD parser never calls
     * it, so nothing was written (fixed in RDF4J 5.0.3).
     */
    @Test
    public void loadJsonLdFilesIntoSeparateGraphs() throws Exception {
        File input = TestUtils.getTempDirectory();
        Files.writeString(new File(input, "a.jsonld").toPath(),
                JSON_LD.formatted("a", "a"));
        Files.writeString(new File(input, "b.jsonld").toPath(),
                JSON_LD.formatted("b", "b"));

        FilesToRdf component = new FilesToRdf();
        component.configuration = new FilesToRdfConfiguration();
        component.configuration.setCommitSize(50000);
        try (TestEnvironment environment = TestEnvironment.create(
                component, TestUtils.getTempDirectory())) {
            environment.bindSystemDataUnit("InputFiles", input);
            TestGraphListDataUnit output =
                    environment.bindGraphListDataUnit("OutputRdf");
            environment.execute();

            List<IRI> graphs = new ArrayList<>(output.getReadGraphs());
            Assertions.assertEquals(2, graphs.size());
            Set<Resource> subjects = new HashSet<>();
            for (IRI graph : graphs) {
                List<Statement> statements = statements(output, graph);
                Assertions.assertEquals(3, statements.size(),
                        "Statements in " + graph + ": " + statements);
                for (Statement statement : statements) {
                    if (!(statement.getSubject() instanceof BNode)) {
                        subjects.add(statement.getSubject());
                    }
                }
            }
            Assertions.assertEquals(2, subjects.size());
        }
    }

    /**
     * A remote context shared by many JSON-LD files must be fetched once, not
     * once per file. RDF4J 5.0.x only used its caching document loader in
     * secure mode, which this component disables, so every file fetched
     * every remote context again; with many files referencing e.g.
     * schema.org this ran the HTTP/2 connection out of streams. RDF4J 5.1
     * caches regardless of secure mode.
     */
    @Test
    public void fetchSharedRemoteContextOnce() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        byte[] context = """
                { "@context": { "title": "http://localhost/title" } }
                """.getBytes(StandardCharsets.UTF_8);
        HttpServer server = HttpServer.create(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/context.jsonld", exchange -> {
            requests.incrementAndGet();
            exchange.getResponseHeaders().add(
                    "Content-Type", "application/ld+json");
            exchange.sendResponseHeaders(200, context.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(context);
            }
        });
        server.start();
        try {
            String contextUrl = "http://127.0.0.1:"
                    + server.getAddress().getPort() + "/context.jsonld";
            int fileCount = 20;
            File input = TestUtils.getTempDirectory();
            for (int index = 0; index < fileCount; ++index) {
                Files.writeString(
                        new File(input, index + ".jsonld").toPath(), """
                        {
                          "@context": "%s",
                          "@id": "http://localhost/%d",
                          "title": "%d"
                        }
                        """.formatted(contextUrl, index, index));
            }

            FilesToRdf component = new FilesToRdf();
            component.configuration = new FilesToRdfConfiguration();
            component.configuration.setCommitSize(50000);
            try (TestEnvironment environment = TestEnvironment.create(
                    component, TestUtils.getTempDirectory())) {
                environment.bindSystemDataUnit("InputFiles", input);
                TestGraphListDataUnit output =
                        environment.bindGraphListDataUnit("OutputRdf");
                environment.execute();

                List<IRI> graphs = new ArrayList<>(output.getReadGraphs());
                Assertions.assertEquals(fileCount, graphs.size());
                for (IRI graph : graphs) {
                    Assertions.assertEquals(1,
                            statements(output, graph).size());
                }
            }
            Assertions.assertEquals(1, requests.get(),
                    "Requests for the shared remote context");
        } finally {
            server.stop(0);
        }
    }

    private static List<Statement> statements(
            TestGraphListDataUnit dataUnit, IRI graph) {
        List<Statement> result = new ArrayList<>();
        try (RepositoryConnection connection =
                     dataUnit.getRepository().getConnection()) {
            connection.getStatements(null, null, null, graph)
                    .forEach(result::add);
        }
        return result;
    }

}
