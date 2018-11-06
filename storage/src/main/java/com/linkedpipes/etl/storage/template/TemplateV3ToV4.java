package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.repository.WritableTemplateRepository;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;

/**
 * Add configuration graph to interface and definition.
 */
class TemplateV3ToV4 {

    private final WritableTemplateRepository repository;

    public TemplateV3ToV4(WritableTemplateRepository repository) {
        this.repository = repository;
    }

    public void migrate(Template template) throws BaseException {
        if (!Template.Type.REFERENCE_TEMPLATE.equals(template.getType())) {
            return;
        }
        updateDefinition(template);
        updateInterface(template);
    }

    private void updateDefinition(Template template)
            throws RdfUtils.RdfException {
        Collection<Statement> statements = repository.getDefinition(template);
        repository.setDefinition(
                template, addConfigurationIri(statements, template.getIri()));
    }

    private Collection<Statement> addConfigurationIri(
            Collection<Statement> statements, String iri) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        statements.add(valueFactory.createStatement(
                valueFactory.createIRI(iri),
                valueFactory.createIRI(LP_PIPELINE.HAS_CONFIGURATION_GRAPH),
                valueFactory.createIRI(
                        ReferenceFactory.createConfigurationIri(iri)),
                valueFactory.createIRI(iri)
        ));
        return statements;
    }

    private void updateInterface(Template template)
            throws RdfUtils.RdfException {
        Collection<Statement> statements = repository.getInterface(template);
        repository.setInterface(template,
                addConfigurationIri(statements, template.getIri()));
    }

}
