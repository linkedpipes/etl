package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class PluginTemplateService {

    private static final Logger LOG =
            LoggerFactory.getLogger(PluginTemplateService.class);

    private final TemplateEvents templateEvents;

    private final TemplateRepository repository;

    public PluginTemplateService(
            TemplateEvents templateEvents,
            TemplateRepository repository) {
        this.templateEvents = templateEvents;
        this.repository = repository;
    }

    public void initialize() throws StorageException {
        LOG.debug("Initializing plugin service ... ");
        int pluginCounter = 0;
        for (Resource resource : repository.listPluginTemplates()) {
            PluginTemplate template;
            try {
                template = repository.loadPluginTemplate(resource);
            } catch (StorageException ex) {
                LOG.warn("Can't load plugin template '{}'.", resource, ex);
                continue;
            }
            templateEvents.onPluginTemplateLoaded(template);
            ++pluginCounter;
        }
        LOG.info("Initializing plugin service ... done with {} templates",
                pluginCounter);
    }

    public Set<PluginTemplate> getPluginTemplates() throws StorageException {
        Set<PluginTemplate> result = new HashSet<>();
        for (Resource resource : repository.listPluginTemplates()) {
            result.add(repository.loadPluginTemplate(resource));
        }
        return result;
    }

}
