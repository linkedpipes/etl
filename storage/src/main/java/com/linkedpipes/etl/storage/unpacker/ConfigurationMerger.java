package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.plugin.configuration.ConfigurationFacade;
import com.linkedpipes.etl.plugin.configuration.InvalidConfiguration;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.unpacker.model.GraphCollection;
import com.linkedpipes.etl.storage.unpacker.model.template.Template;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class ConfigurationMerger {

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
        loadConfigDescription(template.getIri(),
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

    private void loadConfigDescription(String iri, String graph)
            throws BaseException {
        if (graphs.containsKey(graph)) {
            return;
        }
        Collection<Statement> statements =
                templateSource.getConfigurationDescription(iri);
        graphs.put(graph, statements);
    }

    public void copyConfigurationGraphs(String source, String target) {
        graphs.put(target, changeGraph(graphs.get(source), target));
    }

    private Collection<Statement> changeGraph(
            Collection<Statement> statements, String graph) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI graphIri = valueFactory.createIRI(graph);
        return statements.stream().map(s -> valueFactory.createStatement(
                s.getSubject(), s.getPredicate(), s.getObject(), graphIri))
                .collect(Collectors.toList());
    }

    public void merge(
            Template template, List<String> configurations, String targetGraph)
            throws BaseException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        ConfigurationFacade configurationFacade = new ConfigurationFacade();
        List<Statement> description = new ArrayList<>(
                graphs.get(template.getConfigDescriptionGraph()));
        List<Statement> result;
        List<List<Statement>> configurationsRdf = configurations.stream()
                .map(iri -> new ArrayList<>(graphs.get(iri)))
                .collect(Collectors.toList());
        Collections.reverse(configurationsRdf);
        try {
            result = configurationFacade.merge(
                    configurationsRdf,
                    description,
                    targetGraph,
                    valueFactory.createIRI(targetGraph)
            );
        } catch (InvalidConfiguration ex) {
            throw new BaseException(
                    "Can't merge configuration for: {}",
                    template.getIri(), ex);
        }
        graphs.put(targetGraph, result);
    }

}
