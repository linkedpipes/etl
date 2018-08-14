package com.linkedpipes.etl.storage.pipeline.transformation;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.*;

/**
 * Holds information about a single template. Is used for pipeline
 * updates.
 */
class TemplateInfo {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    /**
     * IRI of this template.
     */
    private IRI iri;

    /**
     * IRI of the parent template.
     */
    private IRI template = null;

    /**
     * IRI for the definition graph.
     */
    private IRI definitionGraph;

    /**
     * Definition of this template. Without the reference to the pipeline.
     */
    private final Collection<Statement> definition;

    /**
     * Configuration of this template.
     */
    private Collection<Statement> configuration = null;

    /**
     * Description of configuration for this template.
     */
    private Collection<Statement> configurationDescription = null;

    private TemplateInfo(IRI iri, Collection<Statement> definition) {
        this.iri = iri;
        this.definition = definition;
    }

    public String getIri() {
        return iri.stringValue();
    }

    public IRI getTemplate() {
        return template;
    }

    public void setTemplate(IRI template) {
        this.template = template;
    }

    /**
     * @return Complete definition with created reference to the parent.
     */
    public Collection<Statement> getDefinition() {
        final List<Statement> result = new ArrayList<>(definition.size() + 1);
        result.addAll(definition);
        result.add(VF.createStatement(
                iri, VF.createIRI("http://linkedpipes.com/ontology/template"),
                template, definitionGraph));
        return result;
    }

    public Collection<Statement> getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(Collection<Statement> configuration) {
        this.configuration = configuration;
    }

    public Collection<Statement> getConfigurationDescription() {
        return this.configurationDescription;
    }

    /**
     * Extract information from given graphs about templates and return it.
     *
     * @param graphs
     * @return
     */
    public static List<TemplateInfo> create(Map<IRI, List<Statement>> graphs) {
        final List<TemplateInfo> result = new LinkedList<>();
        //
        for (Map.Entry<IRI, List<Statement>> entry : graphs.entrySet()) {
            TemplateInfo templateInfo = null;
            IRI parentTemplate = null;
            IRI configurationGraph = null;
            IRI configurationDescriptionGraph = null;
            // We may want to remove some statements and replace them
            // later.
            final Collection<Statement> toRemove = new LinkedList<>();
            //
            for (Statement statement : entry.getValue()) {
                if (statement.getPredicate().equals(RDF.TYPE)) {
                    if (statement.getObject().stringValue().equals(
                            "http://linkedpipes.com/ontology/Template")) {
                        templateInfo = new TemplateInfo(
                                entry.getKey(), entry.getValue());
                    }
                } else if (statement.getPredicate().stringValue().equals(
                        LP_PIPELINE.HAS_CONFIGURATION_GRAPH)) {
                    configurationGraph = (IRI) statement.getObject();
                } else if (statement.getPredicate().stringValue().equals(
                        LP_PIPELINE.HAS_TEMPLATE)) {
                    parentTemplate = (IRI) statement.getObject();
                    toRemove.add(statement);
                } else if (statement.getPredicate().stringValue().equals(
                        LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION)) {
                    configurationDescriptionGraph = (IRI) statement.getObject();
                }
            }
            if (templateInfo != null) {
                templateInfo.definition.removeAll(toRemove);
                if (configurationGraph != null) {
                    templateInfo.configuration = graphs.get(configurationGraph);
                }
                if (configurationDescriptionGraph!= null) {
                    templateInfo.configurationDescription =
                            graphs.get(configurationDescriptionGraph);
                }
                templateInfo.template = parentTemplate;
                templateInfo.definitionGraph = entry.getKey();
                result.add(templateInfo);
            }
        }
        return result;
    }

}
