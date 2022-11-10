package com.linkedpipes.etl.storage.template.repository;

import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.TemplateRepository;
import com.linkedpipes.etl.storage.template.repository.file.FileTemplateRepository;
import com.linkedpipes.etl.storage.template.repository.legacy.LegacyTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  Create and return initialized repository. Perform migration if needed.
 */
public class TemplateRepositoryFactory {

    private static final Logger LOG =
            LoggerFactory.getLogger(TemplateRepositoryFactory.class);

    private final List<PluginTemplate> pluginTemplates;

    public TemplateRepositoryFactory(List<PluginTemplate> pluginTemplates) {
        this.pluginTemplates = pluginTemplates;
    }

    /**
     * Create initialized repository.
     */
    public TemplateRepository create(File directory) throws StorageException {
        RepositoryInfo info = loadRepositoryInfo(directory);
        List<ReferenceTemplate> migratedFromLegacy = new ArrayList<>();
        if (isLegacy(info)) {
            migratedFromLegacy = loadFromLegacy(info, directory);
            info = new RepositoryInfo(
                    info.version(),
                    FileTemplateRepository.NAME);
        }
        File templateDirectory = getTemplatesDirectory(directory);
        if (!templateDirectory.exists()) {
            templateDirectory.mkdirs();
        }
        TemplateRepository result;
        switch (info.templateRepository()) {
            case FileTemplateRepository.NAME:
                result = new FileTemplateRepository(templateDirectory);
                break;
            default:
                throw new StorageException(
                        "Unsupported repository type '{}'.",
                        info.templateRepository());
        }
        // Initialize.
        List<StorageException> exceptions = result.initializeAndMigrate();
        for (StorageException exception : exceptions) {
            LOG.error("Error while creating repository.", exception);
        }
        if (!exceptions.isEmpty()) {
            throw new StorageException("Can't initialize repository.");
        }
        storePluginTemplates(result);
        storeReferenceTemplates(migratedFromLegacy, result);
        try {
            info.save(getRepositoryInfoFile(directory));
        } catch (IOException ex) {
            throw new StorageException("Can't save updated info file.", ex);
        }
        return result;
    }

    /**
     * We may need to create new repository info as a result of old
     * repository or new repository.
     */
    private RepositoryInfo loadRepositoryInfo(File directory)
            throws StorageException {
        File file = getRepositoryInfoFile(directory);
        if (file.exists()) {
            return RepositoryInfo.load(file);
        }
        File templates = getTemplatesDirectory(directory);
        if (templates.exists()) {
            File oldFile = new File(templates, "repository-info.json");
            if (oldFile.exists()) {
                return RepositoryInfo.load(oldFile);
            } else {
                return RepositoryInfo.createV0();
            }
        }
        File legacyBackup = getLegacyBackupDirectory(directory);
        if (legacyBackup.exists()) {
            // We have interrupted loading from legacy store.
            return RepositoryInfo.load(getRepositoryInfoFile(
                    getTemplatesDirectory(legacyBackup)));
        } else {
            // Initializing new repository.
            return RepositoryInfo.createNew();
        }
    }

    private File getRepositoryInfoFile(File directory) {
        return new File(directory, "repository-info.json");
    }

    private File getTemplatesDirectory(File directory) {
        return new File(directory, "templates");
    }

    private File getLegacyBackupDirectory(File directory) {
        return new File(directory, "templates-legacy-backup");
    }

    /**
     * Either name of the legacy repository created by default,
     * or null template repository value.
     */
    private boolean isLegacy(RepositoryInfo info) {
        return Objects.equals(info.templateRepository(),
                LegacyTemplateRepository.NAME);
    }

    private List<ReferenceTemplate> loadFromLegacy(
            RepositoryInfo info, File directory) throws StorageException {
        File backupFile;
        try {
            backupFile = backupContent(directory);
        } catch (IOException ex) {
            throw new StorageException("Can't create backup.", ex);
        }
        LOG.info("Created backup of legacy template store in '{}'.",
                backupFile);
        LegacyTemplateRepository legacy = new LegacyTemplateRepository(
                pluginTemplates, info.version());
        return legacy.loadReferenceTemplates(
                getTemplatesDirectory(backupFile),
                getKnowledgeDirectory(backupFile));
    }


    /**
     * Move content to directory and return it.
     */
    private File backupContent(File directory) throws IOException {
        File result = getLegacyBackupDirectory(directory);
        result.mkdirs();
        File templatesSource = getTemplatesDirectory(directory);
        File templatesTarget = getTemplatesDirectory(result);
        if (templatesSource.exists() && !templatesTarget.exists()) {
            Files.move(templatesSource.toPath(), templatesTarget.toPath());
        }
        File knowledgeSource = getKnowledgeDirectory(directory);
        File knowledgeTarget = getKnowledgeDirectory(result);
        if (knowledgeSource.exists() && !knowledgeTarget.exists()) {
            Files.move(knowledgeSource.toPath(), knowledgeTarget.toPath());
        }
        return result;
    }

    private File getKnowledgeDirectory(File directory) {
        return new File(directory, "knowledge");
    }

    private void storePluginTemplates(TemplateRepository repository)
            throws StorageException {
        for (PluginTemplate pluginTemplate : pluginTemplates) {
            repository.storePluginTemplate(pluginTemplate);
        }
    }

    private void storeReferenceTemplates(
            List<ReferenceTemplate> templates,
            TemplateRepository repository) throws StorageException {
        for (ReferenceTemplate template : templates) {
            repository.storeReferenceTemplate(template);
        }
    }

}
