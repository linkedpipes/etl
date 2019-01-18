package com.linkedpipes.etl.dataunit.core.rdf;

import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * For data units in the same group provide the same repository and make sure
 * that the repository is open as long as at least one data unit is using it.
 */
class RepositoryManager {

    private static class RepositoryContainer {

        private final String group;

        private final Repository repository;

        private int useCounter = 0;

        public RepositoryContainer(String group, Repository repository) {
            this.group = group;
            this.repository = repository;
        }

    }

    private static class ManagerConfiguration {

        private final String repositoryPolicy;

        private final String repositoryType;

        private final File workingDirectory;

        public ManagerConfiguration(
                String repositoryPolicy,
                String repositoryType,
                File workingDirectory) {
            this.repositoryPolicy = repositoryPolicy;
            this.repositoryType = repositoryType;
            this.workingDirectory = workingDirectory;
        }

        private boolean isSingleRepository() {
            return LP_PIPELINE.SINGLE_REPOSITORY.equals(repositoryPolicy);
        }

        private boolean isInMemory() {
            return LP_PIPELINE.MEMORY_STORE.equals(this.repositoryType);
        }

    }

    private static final int DELETE_WAIT_TIME = 1000;

    private static final Logger LOG =
            LoggerFactory.getLogger(RepositoryManager.class);

    private final ManagerConfiguration configuration;

    private final Map<String, RepositoryContainer> repositories =
            new HashMap<>();

    public RepositoryManager(
            String repositoryPolicy, String repositoryType, File directory) {
        LOG.info("Repository policy: {}", repositoryPolicy);
        LOG.info("Repository type: {}", repositoryType);
        LOG.info("Directory: {}", directory);
        configuration = new ManagerConfiguration(
                repositoryPolicy, repositoryType, directory);
    }

    public Repository getRepository(DataUnitConfiguration configuration)
            throws LpException {
        String group = configuration.getGroup();
        if (this.configuration.isSingleRepository()) {
            group = "single";
        }
        RepositoryContainer container = getOrCreateRepository(group);
        ++container.useCounter;
        LOG.info("Using repository group: {} ({}) for: {}",
                group, container.useCounter, configuration.getResource());
        return container.repository;
    }

    private RepositoryContainer getOrCreateRepository(String group)
            throws LpException {
        if (repositories.containsKey(group)) {
            return repositories.get(group);
        } else {
            Repository newRepository = getGroupRepository(group);
            RepositoryContainer container =
                    new RepositoryContainer(group, newRepository);
            repositories.put(group, container);
            return container;
        }
    }

    private Repository getGroupRepository(String group) throws LpException {
        Repository repository;
        if (configuration.isInMemory()) {
            repository = createInMemory();
        } else {
            repository = createNativeRepository(group);
        }
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            throw new LpException("Can't create RDF repository.", ex);
        }
        return repository;
    }

    private Repository createNativeRepository(String group) {
        File directory = new File(
                configuration.workingDirectory,
                "dataunit-sesame-" + group);
        return new SailRepository(new NativeStore(directory));
    }

    private Repository createInMemory() {
        return new SailRepository(new MemoryStore());

    }

    public void closeAll() {
        LOG.info("Closing all remaining repositories.");
        for (RepositoryContainer container : repositories.values()) {
            closeRepositoryContainer(container);
        }
        deleteDirectory();
    }

    private void closeRepositoryContainer(RepositoryContainer container) {
        try {
            LOG.info("Closing repository group: {} ({}) ... ",
                    container.group, container.useCounter);
            container.repository.shutDown();
            LOG.info("Closing repository ... done");
        } catch (RepositoryException ex) {
            LOG.error("Can't closeAll repository.", ex);
        }
    }

    public void closeRepository(Repository repository) {
        for (RepositoryContainer container : repositories.values()) {
            if (container.repository != repository) {
                continue;
            }
            LOG.info("Request to close repository group: {} ({})",
                    container.group, container.useCounter);
            --container.useCounter;
            if (container.useCounter == 0) {
                closeRepositoryContainer(container);
                repositories.remove(container.group);
                return;
            }
        }
        LOG.error("Manager was asked to close unmanaged repository.");
    }

    private void deleteDirectory() {
        File directory = this.configuration.workingDirectory;
        LOG.info("Deleting content ...");
        FileUtils.deleteQuietly(directory);
        // It may take some time before the file is released.
        while (directory.exists()) {
            try {
                Thread.sleep(DELETE_WAIT_TIME);
            } catch (InterruptedException ex) {
                LOG.debug("Interrupt ignored while "
                        + "waiting for directory to be deleted.");
            }
            FileUtils.deleteQuietly(directory);
        }
        LOG.info("Deleting content ... done");
    }

}
