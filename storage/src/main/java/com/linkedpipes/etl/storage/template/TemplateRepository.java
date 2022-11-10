package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.repository.Repository;
import org.eclipse.rdf4j.model.Resource;

import java.util.Set;

/**
 * Repository is responsible for data migration.
 */
public interface TemplateRepository extends Repository {

    Set<Resource> listPluginTemplates() throws StorageException;

    PluginTemplate loadPluginTemplate(Resource resource)
            throws StorageException;

    void storePluginTemplate(PluginTemplate template)
        throws StorageException;

    Set<Resource> listReferenceTemplates() throws StorageException;

    ReferenceTemplate loadReferenceTemplate(Resource resource)
            throws StorageException;

    void storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException;

    void deleteReferenceTemplate(Resource resource) throws StorageException;

}
