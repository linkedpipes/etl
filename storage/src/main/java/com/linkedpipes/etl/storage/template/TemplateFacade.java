package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemplateFacade
        implements PluginTemplateFacade, ReferenceTemplateFacade {

    private final Configuration configuration;

    private final ReferenceTemplateService referenceService;

    private final PluginTemplateService pluginService;

    private final TemplateRepository repository;

    public TemplateFacade(
            Configuration configuration,
            ReferenceTemplateService referenceService,
            PluginTemplateService pluginService,
            TemplateRepository repository) {
        this.configuration = configuration;
        this.referenceService = referenceService;
        this.pluginService = pluginService;
        this.repository = repository;
    }

    @Override
    public boolean isPluginTemplate(Resource resource)
            throws StorageException {
        return repository.listPluginTemplates().contains(resource);
    }

    @Override
    public PluginTemplate getPluginTemplate(Resource resource)
            throws StorageException {
        return repository.loadPluginTemplate(resource);
    }

    @Override
    public Set<PluginTemplate> getPluginTemplates()
            throws StorageException {
        return pluginService.getPluginTemplates();
    }

    @Override
    public ReferenceTemplate getReferenceTemplate(Resource resource)
            throws StorageException {
        return repository.loadReferenceTemplate(resource);
    }

    @Override
    public Set<ReferenceTemplate> getReferenceTemplates()
            throws StorageException {
        return referenceService.getReferenceTemplates();
    }

    @Override
    public PluginTemplate findPluginTemplate(Resource resource)
            throws StorageException {
        if (isPluginTemplate(resource)) {
            return getPluginTemplate(resource);
        }
        ReferenceTemplate template = getReferenceTemplate(resource);
        if (template == null) {
            throw new StorageException("Can't find template '{}'.", resource);
        }
        return getPluginTemplate(template.plugin());
    }

    @Override
    public Map<Resource, Resource> getTemplateToPluginMap()
            throws StorageException {
        Map<Resource, Resource> result = new HashMap<>();
        for (ReferenceTemplate template : getReferenceTemplates()) {
            result.put(template.resource(), template.plugin());
        }
        for (Resource resource : repository.listPluginTemplates()) {
            result.put(resource, resource);
        }
        return result;
    }

    @Override
    public void storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException {
        referenceService.storeReferenceTemplate(template);
    }

    @Override
    public void deleteReferenceTemplate(Resource resource)
            throws StorageException {
        referenceService.deleteReferenceTemplate(resource);
    }

    @Override
    public Resource reserveReferenceResource() {
        return repository.reserveResource(
                ReferenceTemplate::createResource,
                configuration.getDomainName());
    }

    @Override
    public void reloadReferenceTemplates() throws StorageException {
        referenceService.reloadReferenceTemplates();
    }


}
