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

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
