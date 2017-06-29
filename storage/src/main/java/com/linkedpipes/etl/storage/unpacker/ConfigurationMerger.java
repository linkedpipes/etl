package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.configuration.ConfigurationFacade;
import com.linkedpipes.etl.storage.unpacker.model.GraphCollection;
import com.linkedpipes.etl.storage.unpacker.model.template.Template;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;

class ConfigurationMerger {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final GraphCollection graphs;

    private final TemplateSource templateSource;

    public ConfigurationMerger(GraphCollection graphs,
            TemplateSource templateSource) {
        this.graphs = graphs;
        this.templateSource = templateSource;
    }

    public void loadTemplateConfigAndDescription(Template template)
            throws BaseException {
        loadConfigTemplate(template.getIri(),
                template.getConfigGraph());
        LoadConfigDescription(template.getIri(),
                template.getConfigDescriptionGraph());
    }

    private void loadConfigTemplate(String iri, String graph)
            throws BaseException {
        if (graphs.containsKey(graph)) {
            return;
        }
        Collection<Statement> statements = templateSource.getConfiguration(iri);
        graphs.put(graph, statements);
    }

    private void LoadConfigDescription(String iri, String graph)
            throws BaseException {
        if (graphs.containsKey(graph)) {
            return;
        }
        Collection<Statement> statements =
                templateSource.getConfigurationDescription(iri);
        graphs.put(graph, statements);
    }

    public void copyConfigurationGraphs(String source, String target) {
        graphs.put(target, graphs.get(source));
    }

    public void mergerAndReplaceConfiguration(Template template,
            String componentConfigurationGraph) throws BaseException {
        Collection<Statement> templateTriples =
                graphs.get(template.getConfigGraph());
        Collection<Statement> componentTriples =
                graphs.get(componentConfigurationGraph);
        Collection<Statement> descriptionTriples =
                graphs.get(template.getConfigDescriptionGraph());

        String baseIri = componentConfigurationGraph + "/";

        Collection<Statement> configuration =
                ConfigurationFacade.mergeFromBottom(
                        templateTriples, componentTriples,
                        descriptionTriples, baseIri,
                        valueFactory.createIRI(componentConfigurationGraph));

        graphs.put(componentConfigurationGraph, configuration);
    }

}
