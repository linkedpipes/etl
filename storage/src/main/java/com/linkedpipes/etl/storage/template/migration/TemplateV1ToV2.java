package com.linkedpipes.etl.storage.template.migration;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.migration.MigrateV1ToV2;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.JarTemplateRef;
import com.linkedpipes.etl.storage.template.ReferenceTemplateRef;
import com.linkedpipes.etl.storage.template.Template;
import com.linkedpipes.etl.storage.template.TemplateService;
import com.linkedpipes.etl.storage.template.repository.WritableTemplateRepository;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateV1ToV2 {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final TemplateService manager;

    private final WritableTemplateRepository repository;

    /**
     * Template being updated.
     */
    private ReferenceTemplateRef template = null;

    public TemplateV1ToV2(
            TemplateService manager, WritableTemplateRepository repository) {
        this.manager = manager;
        this.repository = repository;
    }

    public void migrate(Template template) throws StorageException {
        if (template.getType() == Template.Type.JAR_TEMPLATE) {
            return;
        }
        this.template = (ReferenceTemplateRef) template;
        JarTemplateRef jarTemplate = getJarTemplate();
        if (MigrateV1ToV2.shouldUpdate(jarTemplate.getIri())) {
            // Update configuration.
            updateConfiguration(jarTemplate);
            // Copy description from template - we update it later.
            repository.setConfigDescription(
                    template, repository.getConfigDescription(jarTemplate));
        }
        updateConfigDescriptionReference();
    }

    private JarTemplateRef getJarTemplate() throws StorageException {
        ReferenceTemplateRef reference = template;
        while (true) {
            Template parent = manager.getTemplates().get(
                    reference.getTemplate());
            if (parent == null) {
                throw new StorageException(
                        "Missing parent ('{}') for template: {}",
                        reference.getTemplate(), reference.getIri());
            }
            if (parent.getType() == Template.Type.JAR_TEMPLATE) {
                return (JarTemplateRef) parent;
            }
            reference = (ReferenceTemplateRef) parent;
        }
    }

    private void updateConfiguration(JarTemplateRef jarTemplate)
            throws RdfUtils.RdfException {
        Collection<Statement> configuration =
                this.repository.getConfig(template);
        Collection<Statement> updatedConfiguration =
                MigrateV1ToV2.updateConfiguration(
                        configuration, jarTemplate.getIri());
        this.repository.setConfig(template, updatedConfiguration);
    }

    private void updateConfigDescriptionReference()
            throws RdfUtils.RdfException {
        IRI configDescription = getDescriptionIri();
        updateDefinition(configDescription);
        updateConfigDescription(configDescription);
    }

    private IRI getDescriptionIri() {
        String iriAsStr;
        if (template.getConfigurationDescription() == null) {
            iriAsStr = template.getIri() + "/configurationDescription";
        } else {
            iriAsStr = template.getConfigurationDescription();
        }
        return valueFactory.createIRI(iriAsStr);
    }

    private void updateDefinition(IRI configDescription)
            throws RdfUtils.RdfException {
        IRI templateIri = valueFactory.createIRI(template.getIri());
        IRI hasConfigDescription = valueFactory.createIRI(
                LP_PIPELINE.HAS_CONFIGURATION_ENTITY_DESCRIPTION);
        // Get definition and remove reference to description.
        List<Statement> definition = repository.getDefinition(template)
                .stream()
                .filter((s) -> !s.getPredicate().equals(hasConfigDescription))
                .collect(Collectors.toList());
        // Add reference to new description.
        definition.add(valueFactory.createStatement(
                templateIri,
                hasConfigDescription,
                configDescription,
                templateIri));
        repository.setDefinition(template, definition);
    }

    private void updateConfigDescription(IRI configDescription)
            throws RdfUtils.RdfException {
        Collection<Statement> statements =
                repository.getConfigDescription(template);
        List<Statement> updated = RdfUtils.updateToIriAndGraph(
                statements, configDescription);
        repository.setConfigDescription(template, updated);
    }

}
