package com.linkedpipes.etl.library.template.reference.migration;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.TestUtils;
import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.rdf.RdfToRawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * When comparing two reference templates we need to get rid of configuration
 * as it fail the equality check.
 */
public class MigrateReferenceTemplateTest {

    private final Map<Resource, Resource> templateToPlugin = new HashMap<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public MigrateReferenceTemplateTest() {
        String prefix = "http://etl.linkedpipes.com/resources/components/";
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        templateToPlugin.put(
                valueFactory.createIRI(
                        prefix + "e-textHolder/0.0.0"),
                valueFactory.createIRI(
                        prefix + "e-textHolder/0.0.0"));
        templateToPlugin.put(
                valueFactory.createIRI(
                        prefix + "t-sparqlConstructChunked/0.0.0"),
                valueFactory.createIRI(
                        prefix + "t-sparqlConstructChunked/0.0.0"));
    }

    @Test
    public void loadV1SparqlConstruct() throws Exception {
        var actual = loadAndMigrate(
                "reference/v1-tSparqlConstructChunked.trig");
        var expected = load(
                "reference/v5-tSparqlConstructChunked.trig");

        Assertions.assertEquals(
                expected.configuration.asList(),
                actual.configuration().asList());

        actual = new ReferenceTemplate(
                actual.resource(), actual.version(),
                actual.template(), actual.plugin(),
                actual.label(), actual.description(), actual.note(),
                actual.color(), actual.tags(),
                actual.knownAs(),
                null,
                actual.configurationGraph());

        expected.configuration = null;
        Assertions.assertEquals(expected.toReferenceTemplate(), actual);

        Assertions.assertEquals(expected, actual);

    }

    private ReferenceTemplate loadAndMigrate(String fileName) throws Exception {
        var template = load(fileName);
        return (new MigrateReferenceTemplate(templateToPlugin))
                .migrate(template);
    }

    private RawReferenceTemplate load(String fileName) throws Exception {
        var statements = Statements.wrap(
                TestUtils.statementsFromResource(fileName)).selector();
        var candidates = RdfToRawReferenceTemplate
                .asRawReferenceTemplates(statements);
        Assertions.assertEquals(1, candidates.size());
        return candidates.get(0);
    }

    @Test
    public void loadV2TextHolder() throws Exception {
        var actual = loadAndMigrate(
                "reference/v2-eTextHolder.trig");

        var expected = load(
                "reference/v5-eTextHolder.trig");

        Assertions.assertEquals(
                expected.configuration.asList(),
                actual.configuration().asList());

        actual = new ReferenceTemplate(
                actual.resource(), actual.version(),
                actual.template(), actual.plugin(),
                actual.label(), actual.description(), "Fix this.",
                actual.color(), actual.tags(),
                valueFactory.createIRI("http://example.com/1621875215693"),
                null,
                actual.configurationGraph());

        expected.configuration = null;
        Assertions.assertEquals(expected.toReferenceTemplate(), actual);
    }

    @Test
    public void loadV3TextHolder() throws Exception {
        var actual = loadAndMigrate("reference/v3-eTextHolder.trig");
        var expected = load("reference/v5-eTextHolder.trig");

        Assertions.assertEquals(
                expected.configuration.asList(),
                actual.configuration().asList());

        actual = new ReferenceTemplate(
                actual.resource(), actual.version(),
                actual.template(), actual.plugin(),
                actual.label(), actual.description(), "Fix this.",
                actual.color(), actual.tags(),
                valueFactory.createIRI("http://example.com/1621875215693"),
                null,
                actual.configurationGraph());

        expected.configuration = null;
        Assertions.assertEquals(expected.toReferenceTemplate(), actual);
    }

    @Test
    public void loadV4TextHolder() throws Exception {
        var actual = loadAndMigrate("reference/v4-eTextHolder.trig");
        var expected = load("reference/v5-eTextHolder.trig");

        Assertions.assertEquals(
                expected.configuration.asList(),
                actual.configuration().asList());

        actual = new ReferenceTemplate(
                actual.resource(), actual.version(),
                actual.template(), actual.plugin(),
                actual.label(), actual.description(), "Fix this.",
                actual.color(), actual.tags(),
                valueFactory.createIRI("http://example.com/1621875215693"),
                null,
                actual.configurationGraph());

        expected.configuration = null;
        Assertions.assertEquals(expected.toReferenceTemplate(), actual);
    }

}
