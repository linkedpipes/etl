package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.configuration.ConfigurationFacade;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * @author Petr Škoda
 */
@Service
public class TemplateFacade {

    @Autowired
    private TemplateManager manager;

    public Template getTemplate(String iri) {
        return manager.getTemplates().get(iri);
    }

    public Collection<Template> getTemplates() {
        return (Collection) manager.getTemplates().values();
    }

    /**
     * Return all templates that are ancestors to given template.
     *
     * The core tempalte is the first one.
     *
     * @param templateIri
     * @param includeFirstLevelTemplates
     * @return
     */
    public Collection<Template> getTemplates(String templateIri,
            boolean includeFirstLevelTemplates) {
        Template template = getTemplate(templateIri);
        if (template == null) {
            return Collections.EMPTY_LIST;
        }
        // Search for new.
        final List<Template> templates = new LinkedList<>();
        while (true) {
            if (template instanceof FullTemplate) {
                // Terminal template.
                if (includeFirstLevelTemplates) {
                    templates.add(template);
                }
                break;
            } else if (template instanceof ReferenceTemplate) {
                templates.add(template);
                ReferenceTemplate ref = (ReferenceTemplate) template;
                template = getTemplate(ref.getTemplate());
                if (template == null) {
                    // Missing template.
                    // TODO Throw an exception ?
                    break;
                }
            }
        }
        Collections.reverse(templates);
        return templates;
    }

    public Collection<Statement> getInterface(Template template) {
        return ((BaseTemplate) template).getInterfaceRdf();
    }

    public Collection<Statement> getInterfaces() {
        final Collection<Statement> result = new ArrayList<>();
        for (Template template : manager.getTemplates().values()) {
            result.addAll(((BaseTemplate) template).getInterfaceRdf());
        }
        return result;
    }

    public Collection<Statement> getDefinition(Template template) {
        return ((BaseTemplate) template).getDefinitionRdf();
    }

    public Collection<Statement> getEffectiveConfiguration(Template template)
            throws BaseException {
        final BaseTemplate baseTemplate = (BaseTemplate) template;
        LinkedList<Collection<Statement>> configurations = new LinkedList<>();
        getTemplates(template.getIri(), true).forEach((item) -> {
            configurations.add(getConfigurationTemplate(item));
        });
        //
        if (!baseTemplate.isSupportControl()) {
            // For template without inheritance control, the current
            // configuration is the effective one.
            return ((BaseTemplate) template).getConfigRdf();
        }
        //
        return ConfigurationFacade.merge(configurations,
                baseTemplate.getConfigDescRdf(),
                baseTemplate.getIri() + "/effective/",
                SimpleValueFactory.getInstance().createIRI(
                        baseTemplate.getIri()));
    }

    public Collection<Statement> getConfigurationTemplate(Template template) {
        return ((BaseTemplate) template).getConfigRdf();
    }

    public Collection<Statement> getConfigurationInstance(Template template) {
        return ((BaseTemplate) template).getConfigForInstanceRdf();
    }

    public Collection<Statement> getConfigurationDescription(
            Template template) {
        return ((BaseTemplate) template).getConfigDescRdf();
    }

    public File getDialogResource(Template template, String dialog,
            String path) {
        if (template instanceof FullTemplate) {
            final FullTemplate fullTemplate = (FullTemplate) template;
            // TODO Check for potential security risk!
            return new File(fullTemplate.getDirectory(),
                    "/dialog/" + dialog + "/" + path);
        }
        return null;
    }

    public File getStaticResource(Template template, String path) {
        if (template instanceof FullTemplate) {
            final FullTemplate fullTemplate = (FullTemplate) template;
            // TODO Check for potential security risk!
            return new File(fullTemplate.getDirectory(), "/static/" + path);
        }
        return null;
    }

    /**
     * Create and return a template.
     *
     * @param template Instance to create template from.
     * @param configuration Configuration to used for template.
     * @return
     */
    public Template createTemplate(Collection<Statement> template,
            Collection<Statement> configuration) throws BaseException {
        return manager.createTemplate(template, configuration);
    }

    /**
     * Update template.
     *
     * @param template
     * @param contentRdf
     */
    public void updateTemplate(Template template,
            Collection<Statement> contentRdf) throws BaseException {
        manager.updateTemplate(template, contentRdf);
    }

    /**
     * Update configuration for given component.
     *
     * @param template
     * @param configRdf
     */
    public void updateConfig(Template template,
            Collection<Statement> configRdf) throws BaseException {
        manager.updateConfig(template, configRdf);
    }

}
