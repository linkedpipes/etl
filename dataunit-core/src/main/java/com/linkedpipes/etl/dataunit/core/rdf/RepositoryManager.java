package com.linkedpipes.etl.dataunit.core.rdf;

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

class RepositoryManager {

    private static final Logger LOG =
            LoggerFactory.getLogger(RepositoryManager.class);

    // TODO Replace with a single configuration object.
    private final String repositoryPolicy;

    private final String repositoryType;

    private final File workingDirectory;

    private final Map<String, Repository> groupsRepository = new HashMap<>();

    public RepositoryManager(String repositoryPolicy, String repositoryType,
            File directory) {
        this.repositoryPolicy = repositoryPolicy;
        this.repositoryType = repositoryType;
        LOG.info("Repository policy: {}", repositoryPolicy);
        LOG.info("Repository type: {}", repositoryType);
        this.workingDirectory = directory;
    }

    public Repository getRepository(Configuration configuration)
            throws LpException {
        String group = configuration.getGroup();
        if (repositoryPolicy.equals(LP_PIPELINE.SINGLE_REPOSITORY)) {
            group = "single";
        }
        return getOrCreateRepository(group);
    }

    private Repository getOrCreateRepository(String group) throws LpException {
        if (groupsRepository.containsKey(group)) {
            return groupsRepository.get(group);
        } else {
            Repository newRepository = createRepository(group);
            groupsRepository.put(group, newRepository);
            return newRepository;
        }
    }

    private Repository createRepository(String group) throws LpException {
        Repository repository;
        if (LP_PIPELINE.MEMORY_STORE.equals(repositoryType)) {
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
        File repositoryDirectory = new File(workingDirectory,
                "dataunit-sesame-" + group);
        return new SailRepository(new NativeStore(repositoryDirectory));
    }

    private Repository createInMemory() {
        // TODO Use dataFile ?
        return new SailRepository(new MemoryStore());

    }

    public void close() {
        for (Repository repository : groupsRepository.values()) {
            closeRepository(repository);
        }
        deleteDirectory();
    }

    private void closeRepository(Repository repository) {
        try {
            LOG.info("Closing repository ... ");
            repository.shutDown();
            LOG.info("Closing repository ... done");
        } catch (RepositoryException ex) {
            LOG.error("Can't close repository.", ex);
        }
    }

    private void deleteDirectory() {
        LOG.info("Deleting content ...");
        FileUtils.deleteQuietly(workingDirectory);
        // It may take some time before the file is released.
        while (workingDirectory.exists()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
            }
            FileUtils.deleteQuietly(workingDirectory);
        }
        LOG.info("Deleting content ... done");
    }

}
