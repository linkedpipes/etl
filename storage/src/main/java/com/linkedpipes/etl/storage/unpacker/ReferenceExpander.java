package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.unpacker.model.GraphCollection;
import com.linkedpipes.etl.storage.unpacker.model.designer.DesignerComponent;
import com.linkedpipes.etl.storage.unpacker.model.executor.ExecutorComponent;
import com.linkedpipes.etl.storage.unpacker.model.template.ReferenceTemplate;
import com.linkedpipes.etl.storage.unpacker.model.template.Template;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ReferenceExpander {

    private GraphCollection graphs;

    private final TemplateSource templateSource;

    private final TemplateExpander expander;

    public ReferenceExpander(
            TemplateSource templateSource, TemplateExpander expander) {
        this.templateSource = templateSource;
        this.expander = expander;
    }

    public ExecutorComponent expand(
            DesignerComponent srcComponent, ReferenceTemplate template)
            throws BaseException {
        // The reference template add only a configuration.
        mergeWithTemplate(template, srcComponent);
        DesignerComponent component = new DesignerComponent(srcComponent);
        component.setTemplate(template.getTemplate());
        component.setTypes(Arrays.asList(template.getTemplate()));
        //
        return expander.expand(component);
    }

    private void mergeWithTemplate(
            Template template, DesignerComponent srcComponent)
            throws BaseException {
        String configGraph = srcComponent.getConfigurationGraph();
        if (configGraph == null) {
            copyConfigurationFromTemplate(template, srcComponent);
        } else {
            ConfigurationMerger merger = createMerger(template);
            merger.mergerAndReplaceConfiguration(template, configGraph);
        }
    }

    // TODO Move to a configuration facade ?
    private void copyConfigurationFromTemplate(
            Template template, DesignerComponent srcComponent)
            throws BaseException {
        String configGraph = srcComponent.getIri() + "/configuration";
        List<Statement> statements = new ArrayList<>();
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI graph = valueFactory.createIRI(configGraph);
        for (Statement statement :
                templateSource.getConfiguration(template.getIri())) {
            statements.add(valueFactory.createStatement(
                    statement.getSubject(), statement.getPredicate(),
                    statement.getObject(), graph));
        }
        graphs.put(configGraph, statements);
        srcComponent.setConfigurationGraph(configGraph);
    }

    private ConfigurationMerger createMerger(Template template)
            throws BaseException {
        ConfigurationMerger merger =
                new ConfigurationMerger(graphs, templateSource);
        merger.loadTemplateConfigAndDescription(template);
        return merger;
    }

    public void setGraphs(GraphCollection graphs) {
        this.graphs = graphs;
    }

}
