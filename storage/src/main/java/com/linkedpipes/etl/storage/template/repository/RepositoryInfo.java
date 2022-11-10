package com.linkedpipes.etl.storage.template.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.repository.file.FileTemplateRepository;
import com.linkedpipes.etl.storage.template.repository.legacy.LegacyTemplateRepository;

import java.io.File;
import java.io.IOException;

/**
 * Model class used to represent saved information about a repository.
 */
record RepositoryInfo(
        /*
         * Component version.
         */
        int version,
        /*
         * Template repository type.
         */
        String templateRepository
) {

    public void save(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, this);
    }

    /**
     * Create new info with the latest version.
     */
    public static RepositoryInfo createNew() {
        return new RepositoryInfo(ReferenceTemplate.VERSION,
                FileTemplateRepository.NAME);
    }

    public static RepositoryInfo load(File file) throws StorageException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(file, RepositoryInfo.class);
        } catch (IOException ex) {
            throw new StorageException(
                    "Can't read repository info file '{}'.",
                    file, ex);
        }
    }

    /**
     * For version 0 there was no repository information file.
     */
    public static RepositoryInfo createV0() {
        return new RepositoryInfo(0, LegacyTemplateRepository.NAME);
    }

}
