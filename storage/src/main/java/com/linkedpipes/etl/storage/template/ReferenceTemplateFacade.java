package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.Resource;

import java.util.Map;
import java.util.Set;

public interface ReferenceTemplateFacade {

    /**
     * Return null if no template is found.
     */
    ReferenceTemplate getReferenceTemplate(Resource resource)
            throws StorageException;

    Set<ReferenceTemplate> getReferenceTemplates()
            throws StorageException;

    /**
     * Return plugin template for given template. The template may be reference
     * or plugin template.
     */
    PluginTemplate findPluginTemplate(Resource resource)
            throws StorageException;

    /**
     * Return map for each reference template to a plugin template.
     */
    Map<Resource, Resource> getTemplateToPluginMap()
            throws StorageException;

    void storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException;

    void deleteReferenceTemplate(Resource resource)
            throws StorageException;

    /**
     * Reserve and return new reference template resource.
     */
    Resource reserveReferenceResource();

    void reloadReferenceTemplates() throws StorageException;

}
