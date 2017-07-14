package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.configuration.ConfigurationFacade;
import com.linkedpipes.etl.storage.mapping.MappingFacade;
import com.linkedpipes.etl.storage.unpacker.TemplateSource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * TODO Extract implementation of TemplateSource to external class?
 */
@Service
public class TemplateFacade implements TemplateSource {

    @Autowired
    private TemplateManager manager;

    @Autowired
    private MappingFacade mappingFacade;

    public Template getTemplate(String iri) {
        return manager.getTemplates().get(iri);
    }

    public Collection<Template> getTemplates() {
        return (Collection) manager.getTemplates().values();
    }

    /**
     * Return all templates that are ancestors to given template.
     *
     * The core template is the first one.
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

    /**
     * Return all successors of the template.
     *
     * @param template
     * @return
     */
    public Collection<Template> getTemplateSuccessors(Template template) {
        // TODO We do not have to construct this every time.
        // Create hierarchy of all templates.
        final Map<Template, List<Template>> successors = new HashMap<>();
        for (Template item : getTemplates()) {
            if (!(item instanceof ReferenceTemplate)) {
                continue;
            }
            final ReferenceTemplate ref = (ReferenceTemplate) item;
            final Template parent = getTemplate(ref.getTemplate());
            List<Template> brothers = successors.get(parent);
            if (brothers == null) {
                brothers = new LinkedList<>();
                successors.put(parent, brothers);
            }
            brothers.add(ref);
        }
        // Gather the results.
        final Set<Template> results = new HashSet<>();
        final Set<Template> toTest = new HashSet<>();
        toTest.addAll(
                successors.getOrDefault(template, Collections.EMPTY_LIST));
        while (!toTest.isEmpty()) {
            final Template item = toTest.iterator().next();
            toTest.remove(item);
            if (results.contains(item)) {
                continue;
            }
            //
            final List<Template> children = successors.getOrDefault(item,
                    Collections.EMPTY_LIST);
            results.add(item);
            results.addAll(children);
            toTest.addAll(children);
        }
        return results;
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

    public void remove(Template template) throws BaseException {
        manager.remove(template);
        mappingFacade.remove(template);
    }

    @Override
    public Collection<Statement> getDefinition(String iri)
            throws BaseException {
        return getDefinition(getTemplate(iri));
    }

    @Override
    public Collection<Statement> getConfiguration(String iri)
            throws BaseException {
        return getConfigurationTemplate(getTemplate(iri));
    }

    @Override
    public Collection<Statement> getConfigurationDescription(String iri)
            throws BaseException {
        return getConfigurationDescription(getTemplate(iri));
    }

}
