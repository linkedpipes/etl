package com.linkedpipes.etl.storage.pipeline.repository;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.pipeline.PipelineRepository;
import com.linkedpipes.etl.storage.pipeline.repository.file.FilePipelineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Create and return initialized repository.
 */
public class PipelineRepositoryFactory {

    private static final Logger LOG =
            LoggerFactory.getLogger(PipelineRepositoryFactory.class);

    public PipelineRepository  create(
            File directory,
            PipelineRepository.TemplateToPlugin templateToPlugin)
            throws StorageException {
        FilePipelineRepository result =
                new FilePipelineRepository(directory, templateToPlugin);
        // Initialize.
        List<StorageException> exceptions = result.initializeAndMigrate();
        for (StorageException exception : exceptions) {
            LOG.error("Error while creating repository.", exception);
        }
        return result;
    }

}
