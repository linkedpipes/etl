package com.linkedpipes.etl.storage.template;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.plugin.model.PluginType;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TemplateBundlerTest {

    private static final SimpleValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    private static final IRI PLUGIN_IRI = VALUE_FACTORY.createIRI("http://localhost/plugin");

    private static final IRI REFERENCE_IRI = VALUE_FACTORY.createIRI("http://localhost/reference");

    private PluginTemplate pluginTemplate(IRI iri) {
        IRI configGraph = VALUE_FACTORY.createIRI(iri.stringValue() + "/configuration");
        IRI configDescriptionGraph = VALUE_FACTORY.createIRI(iri.stringValue() + "/configuration-description");
        ConfigurationDescription description =
                new ConfigurationDescription(configDescriptionGraph, null, null, Map.of());
        return new PluginTemplate(
                iri,
                "Test plugin",
                "#000000",
                PluginType.TRANSFORMER,
                false,
                List.of(),
                null,
                Map.of(),
                List.of(),
                Statements.arrayList(),
                configGraph,
                description,
                configDescriptionGraph);
    }

    private ReferenceTemplate referenceTemplate(Resource iri, Resource plugin) {
        Resource configGraph = VALUE_FACTORY.createIRI(iri.stringValue() + "/configuration");
        return new ReferenceTemplate(
                iri, 1, plugin, plugin, "Test reference", null, null, null, List.of(), null,
                Statements.arrayList(), configGraph);
    }

    @Test
    public void embedsDirectlyReferencedPluginTemplate() throws Exception {
        TemplateFacade templateFacade = Mockito.mock(TemplateFacade.class);
        when(templateFacade.isPluginTemplate(PLUGIN_IRI)).thenReturn(true);
        when(templateFacade.getPluginTemplate(PLUGIN_IRI)).thenReturn(pluginTemplate(PLUGIN_IRI));

        StatementsBuilder pipeline = Statements.arrayList().builder();
        pipeline.addIri(
                "http://localhost/pipeline/component",
                "http://linkedpipes.com/ontology/template",
                PLUGIN_IRI.stringValue());

        Statements result = new TemplateBundler(templateFacade).bundle(pipeline);

        Assertions.assertTrue(result.selector().selectByGraph(PLUGIN_IRI.stringValue()).stream()
                .anyMatch(s -> s.getSubject().equals(PLUGIN_IRI)));
    }

    @Test
    public void followsReferenceTemplateToUnderlyingPlugin() throws Exception {
        TemplateFacade templateFacade = Mockito.mock(TemplateFacade.class);
        when(templateFacade.isPluginTemplate(REFERENCE_IRI)).thenReturn(false);
        when(templateFacade.getReferenceTemplate(REFERENCE_IRI))
                .thenReturn(referenceTemplate(REFERENCE_IRI, PLUGIN_IRI));
        when(templateFacade.isPluginTemplate(PLUGIN_IRI)).thenReturn(true);
        when(templateFacade.getPluginTemplate(PLUGIN_IRI)).thenReturn(pluginTemplate(PLUGIN_IRI));

        StatementsBuilder pipeline = Statements.arrayList().builder();
        pipeline.addIri(
                "http://localhost/pipeline/component",
                "http://linkedpipes.com/ontology/template",
                REFERENCE_IRI.stringValue());

        Statements result = new TemplateBundler(templateFacade).bundle(pipeline);

        Assertions.assertFalse(
                result.selector().selectByGraph(REFERENCE_IRI.stringValue()).isEmpty());
        Assertions.assertFalse(
                result.selector().selectByGraph(PLUGIN_IRI.stringValue()).isEmpty());
        verify(templateFacade, times(1)).getPluginTemplate(PLUGIN_IRI);
    }

    @Test
    public void doesNotEmbedSameTemplateTwice() throws Exception {
        TemplateFacade templateFacade = Mockito.mock(TemplateFacade.class);
        when(templateFacade.isPluginTemplate(PLUGIN_IRI)).thenReturn(true);
        when(templateFacade.getPluginTemplate(PLUGIN_IRI)).thenReturn(pluginTemplate(PLUGIN_IRI));

        StatementsBuilder pipeline = Statements.arrayList().builder();
        pipeline.addIri(
                "http://localhost/pipeline/componentA",
                "http://linkedpipes.com/ontology/template",
                PLUGIN_IRI.stringValue());
        pipeline.addIri(
                "http://localhost/pipeline/componentB",
                "http://linkedpipes.com/ontology/template",
                PLUGIN_IRI.stringValue());

        new TemplateBundler(templateFacade).bundle(pipeline);

        verify(templateFacade, times(1)).getPluginTemplate(PLUGIN_IRI);
    }
}
