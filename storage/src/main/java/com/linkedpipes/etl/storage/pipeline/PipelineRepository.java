package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.repository.Repository;
import org.eclipse.rdf4j.model.Resource;

import java.util.Map;
import java.util.Set;

/**
 * Provide access to contemporary version of the pipeline.
 */
public interface PipelineRepository extends Repository {

    interface TemplateToPlugin {

        Map<Resource, Resource> getTemplateToPluginMap()
                throws StorageException;

    }

    Set<Resource> listPipelines() throws StorageException;

    Pipeline loadPipeline(Resource resource) throws StorageException;

    void storePipeline(Pipeline pipeline) throws StorageException;

    void deletePipeline(Resource resource) throws StorageException;

}
