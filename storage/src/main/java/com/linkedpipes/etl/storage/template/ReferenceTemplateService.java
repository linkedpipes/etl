package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ReferenceTemplateService {

    private static final Logger LOG =
            LoggerFactory.getLogger(ReferenceTemplateService.class);

    private final TemplateEvents templateEvents;

    private final TemplateRepository repository;

    public ReferenceTemplateService(
            TemplateEvents templateEvents,
            TemplateRepository repository) {
        this.templateEvents = templateEvents;
        this.repository = repository;
    }

    public void initialize() throws StorageException {
        LOG.debug("Initializing reference service ... ");
        int pluginCounter = 0;
        for (Resource resource : repository.listReferenceTemplates()) {
            ReferenceTemplate template;
            try {
                template = repository.loadReferenceTemplate(resource);
            } catch (StorageException ex) {
                LOG.warn("Can't load plugin template '{}'.", resource, ex);
                continue;
            }
            templateEvents.onReferenceTemplateLoad(template);
            ++pluginCounter;
        }
        LOG.info("Initializing reference service ... done with {} templates",
                pluginCounter);
    }

    public Set<ReferenceTemplate> getReferenceTemplates()
            throws StorageException {
        Set<ReferenceTemplate> result = new HashSet<>();
        for (Resource resource : repository.listReferenceTemplates()) {
            result.add(repository.loadReferenceTemplate(resource));
        }
        return result;
    }

    /**
     * Create new reference template with given resource.
     */
    public void createReferenceTemplate(
            ReferenceTemplate template, Resource resource)
            throws StorageException {
        PluginTemplate pluginTemplate = repository.loadPluginTemplate(
                template.plugin());
        template = (new ChangeReferenceTemplateResource())
                .localize(pluginTemplate, template, resource);
        repository.storeReferenceTemplate(template);
        templateEvents.onReferenceTemplateCreated(template);
    }

    public void storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException {
        if (template.resource() instanceof BNode) {
            throw new StorageException(
                    "Template resource can not be a blank node.");
        }
        ReferenceTemplate previous = repository.loadReferenceTemplate(
                template.resource());
        if (previous == null) {
            createReferenceTemplate(template, template.resource());
        } else {
            updateReferenceTemplate(previous, template);
        }
    }

    public void updateReferenceTemplate(
            ReferenceTemplate previous, ReferenceTemplate next)
            throws StorageException {
        repository.storeReferenceTemplate(next);
        templateEvents.onReferenceTemplateUpdated(previous, next);
    }

    public void deleteReferenceTemplate(Resource resource)
            throws StorageException {
        ReferenceTemplate template = repository.loadReferenceTemplate(resource);
        if (template == null) {
            return;
        }
        repository.deleteReferenceTemplate(resource);
        templateEvents.onReferenceTemplateDeleted(template);
    }

    /**
     * Force reload.
     */
    public void reloadReferenceTemplates() throws StorageException {
        templateEvents.onReferenceTemplateReload();
        repository.reload();
        initialize();
    }

}
