package com.linkedpipes.etl.unpacker;

import com.linkedpipes.etl.library.template.plugin.adapter.PluginTemplateToRdf;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.ReferenceTemplateToRdf;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;


public class TemplateSource {

    private final TemplateFacade templateFacade;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public TemplateSource(TemplateFacade templateFacade) {
        this.templateFacade = templateFacade;
    }

    public Collection<Statement> getDefinition(String iri)
            throws StorageException {
        Resource resource = valueFactory.createIRI(iri);
        if (templateFacade.isPluginTemplate(resource)) {
            PluginTemplate template =
                    templateFacade.getPluginTemplate(resource);
            if (template == null) {
                throw new StorageException("Template '{}' not found.", iri);
            }
            return PluginTemplateToRdf.definitionAsRdf(template);
        } else {
            ReferenceTemplate template =
                    templateFacade.getReferenceTemplate(resource);
            if (template == null) {
                throw new StorageException("Template '{}' not found.", iri);
            }
            return ReferenceTemplateToRdf.definitionAsRdf(template);
        }
    }

    public Collection<Statement> getConfiguration(String iri)
            throws StorageException {
        Resource resource = valueFactory.createIRI(iri);
        if (templateFacade.isPluginTemplate(resource)) {
            PluginTemplate template =
                    templateFacade.getPluginTemplate(resource);
            if (template == null) {
                throw new StorageException("Template '{}' not found.", iri);
            }
            return PluginTemplateToRdf.configurationAsRdf(template);
        } else {
            ReferenceTemplate template =
                    templateFacade.getReferenceTemplate(resource);
            if (template == null) {
                throw new StorageException("Template '{}' not found.", iri);
            }
            return ReferenceTemplateToRdf.configurationAsRdf(template);
        }
    }

    public Collection<Statement> getConfigurationDescription(String iri)
            throws StorageException {
        PluginTemplate plugin =
                templateFacade.findPluginTemplate(valueFactory.createIRI(iri));
        return PluginTemplateToRdf.configurationDescriptionAsRdf(plugin);
    }

}
