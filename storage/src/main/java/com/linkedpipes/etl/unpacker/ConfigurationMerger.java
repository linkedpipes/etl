package com.linkedpipes.etl.unpacker;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.configuration.ConfigurationException;
import com.linkedpipes.etl.library.template.configuration.ConfigurationFacade;
import com.linkedpipes.etl.library.template.configuration.adapter.RdfToConfigurationDescription;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.unpacker.model.GraphCollection;
import com.linkedpipes.etl.unpacker.model.template.Template;
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
            throws StorageException {
        loadConfigTemplate(template.getIri(),
                template.getConfigGraph());
        loadConfigDescription(template.getIri(),
                template.getConfigDescriptionGraph());
    }

    private void loadConfigTemplate(String iri, String graph)
            throws StorageException {
        if (graphs.containsKey(graph)) {
            return;
        }
        Collection<Statement> statements = templateSource.getConfiguration(iri);
        graphs.put(graph, statements);
    }

    private void loadConfigDescription(String iri, String graph)
            throws StorageException {
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
            throws StorageException {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        List<Statement> description = new ArrayList<>(
                graphs.get(template.getConfigDescriptionGraph()));
        List<Statement> result;
        List<List<Statement>> configurationsRdf = configurations.stream()
                .map(iri -> new ArrayList<>(graphs.get(iri)))
                .collect(Collectors.toList());
        Collections.reverse(configurationsRdf);

        List<ConfigurationDescription> candidateDescriptions =
                RdfToConfigurationDescription.asConfigurationDescriptions(
                        Statements.wrap(description).selector());
        if (candidateDescriptions.size() != 1) {
            throw new StorageException(
                    "Invalid number of descriptions for '{}'.",
                    template.getIri());
        }
        try {
            result = ConfigurationFacade.merge(
                    configurationsRdf,
                    candidateDescriptions.get(0),
                    targetGraph,
                    valueFactory.createIRI(targetGraph)
            );
        } catch (ConfigurationException ex) {
            throw new StorageException(
                    "Can't merge configurations for '{}'.",
                    template.getIri(), ex);
        }
        graphs.put(targetGraph, result);
    }

}
