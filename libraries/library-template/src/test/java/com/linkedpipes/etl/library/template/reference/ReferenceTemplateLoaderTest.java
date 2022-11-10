package com.linkedpipes.etl.library.template.reference;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.TestUtils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReferenceTemplateLoaderTest {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void loadMultipleTemplate() throws Exception {
        Set<Resource> plugins = new HashSet<>();
        plugins.add(valueFactory.createIRI(
                "http://etl.linkedpipes.com/resources/components/" +
                        "e-textHolder/0.0.0"));
        plugins.add(valueFactory.createIRI(
                "http://etl.linkedpipes.com/resources/components/" +
                        "t-sparqlConstructChunked/0.0.0"));
        Map<Resource, Resource> map = new HashMap<>();
        ReferenceTemplateLoader loader =
                new ReferenceTemplateLoader(plugins, map);
        Statements statements = Statements.arrayList();
        statements.file().addAll(TestUtils.file(
                "reference/loader-000.trig"));
        // There is separate definition and interface, in same graph
        // using the same resource. There is missing version.
        statements.file().addAll(TestUtils.file(
                "reference/v4-eTextHolder.trig"));
        statements.file().addAll(TestUtils.file(
                "reference/v5-tSparqlConstructChunked.trig"));
        loader.loadAndMigrate(statements);

        Assertions.assertFalse(loader.hasAnyFailed());

        var containers = loader.getContainers();
        Assertions.assertEquals(2, containers.size());

        var migrated = loader.getMigratedTemplates();
        Assertions.assertEquals(1, migrated.size());

        // TODO We could check the content here.

    }

}
