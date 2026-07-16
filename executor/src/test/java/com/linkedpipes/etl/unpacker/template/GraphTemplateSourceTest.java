package com.linkedpipes.etl.unpacker.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.unpacker.UnpackerException;
import java.util.Collection;
import org.eclipse.rdf4j.model.Statement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GraphTemplateSourceTest {

    private static final String TEMPLATE_IRI = "http://localhost/template";

    private static final String CONFIG_GRAPH = TEMPLATE_IRI + "/configuration";

    private static final String CONFIG_DESCRIPTION_GRAPH = TEMPLATE_IRI + "/configuration/desc";

    private Statements bundle() {
        StatementsBuilder builder = Statements.arrayList().builder();

        builder.setDefaultGraph(TEMPLATE_IRI);
        builder.addType(TEMPLATE_IRI, LP_PIPELINE.JAS_TEMPLATE);
        builder.addIri(TEMPLATE_IRI, LP_PIPELINE.HAS_JAR_URL, "http://localhost/jar");
        builder.addIri(TEMPLATE_IRI, LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION, CONFIG_DESCRIPTION_GRAPH);

        builder.setDefaultGraph(CONFIG_GRAPH);
        builder.addType(TEMPLATE_IRI + "/configuration/instance", "http://localhost/Configuration");

        builder.setDefaultGraph(CONFIG_DESCRIPTION_GRAPH);
        builder.addType(CONFIG_DESCRIPTION_GRAPH, "http://plugins.linkedpipes.com/ontology/ConfigurationDescription");

        return builder;
    }

    @Test
    public void resolveTemplateFromEmbeddedGraphs() throws Exception {
        GraphTemplateSource source = new GraphTemplateSource(bundle().selector());

        Collection<Statement> definition = source.getDefinition(TEMPLATE_IRI);
        Assertions.assertFalse(definition.isEmpty());

        Collection<Statement> configuration = source.getConfiguration(TEMPLATE_IRI);
        Assertions.assertFalse(configuration.isEmpty());

        Collection<Statement> configurationDescription = source.getConfigurationDescription(TEMPLATE_IRI);
        Assertions.assertFalse(configurationDescription.isEmpty());
    }

    @Test
    public void missingGraphRaisesUnpackerException() {
        Statements empty = Statements.arrayList();
        GraphTemplateSource source = new GraphTemplateSource(empty.selector());
        Assertions.assertThrows(UnpackerException.class, () -> source.getDefinition(TEMPLATE_IRI));
    }
}
