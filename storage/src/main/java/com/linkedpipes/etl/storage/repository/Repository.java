package com.linkedpipes.etl.storage.repository;

import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.Resource;

import java.util.List;

public interface Repository {

    @FunctionalInterface
    interface ResourceFactory {

        Resource apply(String baseUrl, String suffix);

    }

    /**
     * Initialize repository and perform data migration. Return list
     * of non-critical exceptions. Throw and exception if repository can not
     * be initialized.
     */
    List<StorageException> initializeAndMigrate() throws StorageException;

    /**
     * Reload data from storage. This operation is called at runtime
     * and thus can not critically fail. It can return list of exceptions.
     * As other methods can be called on the repository, the repository
     * is expected to synchronize on an internal primitive.
     *
     * This operation should be used with caution.
     */
    List<StorageException> reload();

    /**
     * Create new uniq resource using given factory method. Calling this
     * method multiple times must not return same resources.
     */
    Resource reserveResource(ResourceFactory factory, String baseUrl);

}
