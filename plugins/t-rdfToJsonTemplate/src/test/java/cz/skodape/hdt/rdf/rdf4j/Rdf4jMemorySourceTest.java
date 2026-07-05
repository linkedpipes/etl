package cz.skodape.hdt.rdf.rdf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import cz.skodape.hdt.core.ArrayReference;
import cz.skodape.hdt.rdf.rdf4j.model.Rdf4jArray;
import cz.skodape.hdt.rdf.rdf4j.model.Rdf4jReference;
import cz.skodape.hdt.rdf.rdf4j.model.Rdf4jResource;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Rdf4jMemorySourceTest {

    private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    @TempDir
    Path tempDirectory;

    @Test
    void reversePropertyUsesInputGraphForResourceReferences() throws Exception {
        Path inputFile = tempDirectory.resolve("reverse-property-resource.trig");
        Files.writeString(inputFile, """
                @prefix ex: <http://example.com/> .

                ex:g1 {
                    ex:source1 ex:predicate ex:target .
                }

                ex:g2 {
                    ex:source2 ex:predicate ex:target .
                }
                """);

        Rdf4jMemorySource source = openSource(inputFile, true);
        try {
            IRI graph = VALUE_FACTORY.createIRI("http://example.com/g1");
            IRI target = VALUE_FACTORY.createIRI("http://example.com/target");
            ArrayReference result =
                    source.reverseProperty(new Rdf4jResource(graph, target), "http://example.com/predicate");

            Rdf4jArray array = assertInstanceOf(Rdf4jArray.class, result);
            assertEquals(1, array.getReferences().size());
            Rdf4jReference item = array.getReferences().get(0);
            Rdf4jResource resource = assertInstanceOf(Rdf4jResource.class, item);
            assertEquals(graph, resource.getGraph());
            assertEquals(VALUE_FACTORY.createIRI("http://example.com/source1"), resource.getResource());
        } finally {
            source.close();
        }
    }

    private static Rdf4jMemorySource openSource(Path inputFile, boolean graphAware) throws Exception {
        Rdf4jMemorySourceConfiguration configuration = new Rdf4jMemorySourceConfiguration();
        configuration.file = inputFile.toFile();
        configuration.graphAware = graphAware;

        Rdf4jMemorySource source = new Rdf4jMemorySource(configuration);
        source.open();
        return source;
    }
}
