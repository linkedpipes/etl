package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.vocabulary.SKOS;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.repository.RepositoryReference;
import com.linkedpipes.etl.storage.template.repository.WritableTemplateRepository;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Create new component from user provided input.
 */
class ReferenceFactory {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final TemplateManager manager;

    private final WritableTemplateRepository repository;

    private Resource templateResource = null;

    private String label = null;

    private IRI iri;

    private IRI configIri;

    public ReferenceFactory(
            TemplateManager manager, WritableTemplateRepository repository) {
        this.manager = manager;
        this.repository = repository;
    }

    public ReferenceTemplate create(
            Collection<Statement> content,
            Collection<Statement> config,
            Collection<Statement> description,
            String id, String iriAsString)
            throws BaseException {
        clear();
        parseContent(content);
        iri = valueFactory.createIRI(iriAsString);
        configIri = valueFactory.createIRI(
                createConfigurationIri(iriAsString));
        //
        List<Statement> interfaceRdf = createInterface();
        List<Statement> definitionRdf = createDefinition();
        List<Statement> configRdf = updateConfig(config);
        //
        RepositoryReference ref = RepositoryReference.createReference(id);
        repository.setInterface(ref, interfaceRdf);
        repository.setDefinition(ref, definitionRdf);
        repository.setConfig(ref, configRdf);
        if (description == null) {
            // Create without description, ReferenceTemplates.
        } else {
            repository.setConfigDescription(ref, description);
        }
        //
        TemplateLoader loader = new TemplateLoader(repository);
        return loader.loadReferenceTemplate(
                RepositoryReference.createReference(id));
    }

    public static String createConfigurationIri(String templateIri) {
        return templateIri + "/configuration";
    }

    private void clear() {
        templateResource = null;
        label = null;
    }

    private void parseContent(Collection<Statement> statements)
            throws BaseException {
        Resource resource = RdfUtils.find(statements, ReferenceTemplate.TYPE);
        if (resource == null) {
            throw new BaseException("Missing resource of reference type");
        }
        for (Statement statement : statements) {
            switch (statement.getPredicate().stringValue()) {
                case LP_PIPELINE.HAS_TEMPLATE:
                    templateResource = (Resource) statement.getObject();
                    break;
                case SKOS.PREF_LABEL:
                    label = statement.getObject().stringValue();
                    break;
                default:
                    break;
            }
        }
        if (templateResource == null) {
            throw new BaseException("Missing template reference.");
        }
    }

    private List<Statement> createInterface() {
        List<Statement> output = new ArrayList<>();
        output.add(valueFactory.createStatement(iri,
                RDF.TYPE, ReferenceTemplate.TYPE, iri));
        output.add(valueFactory.createStatement(iri,
                valueFactory.createIRI(LP_PIPELINE.HAS_TEMPLATE),
                templateResource, iri));
        output.add(valueFactory.createStatement(iri,
                valueFactory.createIRI(SKOS.PREF_LABEL),
                valueFactory.createLiteral(label), iri));
        output.add(valueFactory.createStatement(iri,
                valueFactory.createIRI(LP_PIPELINE.HAS_CONFIGURATION_GRAPH),
                configIri, iri));
        return output;
    }

    private List<Statement> createDefinition() {
        List<Statement> output = new ArrayList<>();
        output.add(valueFactory.createStatement(iri,
                RDF.TYPE, ReferenceTemplate.TYPE, iri));
        output.add(valueFactory.createStatement(iri,
                valueFactory.createIRI(LP_PIPELINE.HAS_TEMPLATE),
                templateResource, iri));
        output.add(valueFactory.createStatement(iri,
                valueFactory.createIRI(LP_PIPELINE.HAS_CONFIGURATION_GRAPH),
                configIri, iri));
        return output;
    }

    private List<Statement> updateConfig(Collection<Statement> input) {
        if (input == null || input.isEmpty()) {
            return Collections.emptyList();
        }
        return RdfUtils.updateToIriAndGraph(input, configIri);
    }

}
