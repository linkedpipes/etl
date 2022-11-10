package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.Resource;

import java.util.Set;

public interface PluginTemplateFacade {

    boolean isPluginTemplate(Resource resource) throws StorageException;

    /**
     * Return null if no template is found.
     */
    PluginTemplate getPluginTemplate(Resource resource)
            throws StorageException;

    Set<PluginTemplate> getPluginTemplates()
            throws StorageException;

}
