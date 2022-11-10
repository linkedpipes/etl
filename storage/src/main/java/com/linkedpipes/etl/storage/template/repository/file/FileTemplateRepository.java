package com.linkedpipes.etl.storage.template.repository.file;

import com.linkedpipes.etl.library.rdf.ResourceToString;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.ReferenceTemplateLoader;
import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.RdfToRawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.ReferenceTemplateToRdf;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.template.TemplateRepository;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keep plugin templates in memory and store each reference templates
 * in a single file. In addition, memory cache is used for reference templates.
 */
public class FileTemplateRepository implements TemplateRepository {

    private static final Logger LOG =
            LoggerFactory.getLogger(FileTemplateRepository.class);

    public static final String NAME = "file";

    private final File directory;

    /**
     * We keep those in memory.
     */
    private final Map<Resource, PluginTemplate> pluginTemplates =
            new HashMap<>();

    private final AtomicInteger counter = new AtomicInteger();

    private Map<Resource, File> referenceFiles = new HashMap<>();

    public FileTemplateRepository(File directory) {
        this.directory = directory;
    }

    @Override
    public List<StorageException> initializeAndMigrate() {
        directory.mkdirs();
        return reload();
    }

    @Override
    public List<StorageException> reload() {
        return loadAndMigrate();
    }

    private List<StorageException> loadAndMigrate() {
        LOG.debug("Loading repository ... ");
        Map<Resource, File> nextReferenceFiles = new HashMap<>();
        List<StorageException> result = new ArrayList<>();
        List<File> files = listReferenceTemplateFiles();
        List<RawReferenceTemplate> rawTemplates = new ArrayList<>(files.size());
        for (File file : files) {
            RawReferenceTemplate rawTemplate;
            try {
                rawTemplate = loadRawTemplate(file);
            } catch (StorageException ex) {
                result.add(new StorageException(
                        "Can't load from '{}'.", file, ex));
                continue;
            }
            rawTemplates.add(rawTemplate);
            nextReferenceFiles.put(rawTemplate.resource, file);
        }
        ReferenceTemplateLoader loader = new ReferenceTemplateLoader(
                pluginTemplates.keySet(), Collections.emptyMap());
        loader.loadAndMigrate(rawTemplates);
        loader.getContainers().stream()
                .filter(ReferenceTemplateLoader.Container::isFailed)
                .forEach(container -> result.add(new StorageException(
                        "Can't load template '{}'.",
                        container.rawTemplate().resource,
                        container.exception())));
        // Save migrated templates.
        for (ReferenceTemplate template : loader.getMigratedTemplates()) {
            try {
                File file = nextReferenceFiles.get(template.resource());
                handleMigrated(template, file);
            } catch (StorageException ex) {
                result.add(ex);
            }
        }
        referenceFiles = nextReferenceFiles;
        LOG.debug("Loading repository ... done");
        return result;
    }

    private List<File> listReferenceTemplateFiles() {
        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(files)
                .filter(File::isFile)
                .filter(file -> file.getName().endsWith(".trig"))
                .toList();
    }

    private RawReferenceTemplate loadRawTemplate(File file)
            throws StorageException {
        Statements statements = Statements.arrayList();
        try {
            statements.file().addAllIfExists(file);
        } catch (IOException ex) {
            throw new StorageException("Can't read file.", ex);
        }
        List<RawReferenceTemplate> candidates =
                RdfToRawReferenceTemplate.asRawReferenceTemplates(
                        statements.selector());
        if (candidates.size() != 1) {
            throw new StorageException(
                    "Invalid number of components '{}', expected one in '{}'.",
                    candidates.size(), file);
        }
        return candidates.get(0);
    }

    private void handleMigrated(ReferenceTemplate template, File file)
            throws StorageException {
        try {
            writeTemplate(template, file);
        } catch (StorageException ex) {
            throw new StorageException(
                    "Can't save migrated template '{}'.",
                    template.resource());
        }
    }

    private void writeTemplate(ReferenceTemplate template, File file)
            throws StorageException {
        referenceFiles.put(template.resource(), file);
        Statements statements = Statements.arrayList();
        statements.addAll(
                ReferenceTemplateToRdf.definitionAsRdf(template)
                        .withGraph(template.resource()));
        statements.addAll(
                ReferenceTemplateToRdf.configurationAsRdf(template)
                        .withGraph(template.configurationGraph()));
        try {
            statements.file().writeToFile(file, RDFFormat.TRIG);
        } catch (IOException ex) {
            throw new StorageException("Can't store template.", ex);
        }
    }

    @Override
    public Set<Resource> listPluginTemplates() {
        return pluginTemplates.keySet();
    }

    @Override
    public PluginTemplate loadPluginTemplate(Resource resource) {
        return pluginTemplates.get(resource);
    }

    @Override
    public void storePluginTemplate(PluginTemplate template) {
        pluginTemplates.put(template.resource(), template);
    }

    @Override
    public Set<Resource> listReferenceTemplates() {
        return referenceFiles.keySet();
    }

    @Override
    public ReferenceTemplate loadReferenceTemplate(Resource resource)
            throws StorageException {
        File file = referenceFiles.get(resource);
        if (file == null) {
            return null;
        }
        RawReferenceTemplate rawTemplate = loadRawTemplate(file);
        return rawTemplate.toReferenceTemplate();
    }

    @Override
    public void storeReferenceTemplate(ReferenceTemplate template)
            throws StorageException {
        File file = referenceFiles.computeIfAbsent(
                template.resource(), key -> createNewFile(template));
        writeTemplate(template, file);
    }

    private File createNewFile(ReferenceTemplate template) {
        String fileName = ResourceToString.asBase64Full(template.resource());
        return new File(directory, fileName + ".trig");
    }

    @Override
    public void deleteReferenceTemplate(Resource resource)
            throws StorageException {
        File file = referenceFiles.get(resource);
        if (!file.delete()) {
            throw new StorageException(
                    "Can't delete pipeline file '{}'.", file);
        }
        referenceFiles.remove(resource);
    }

    @Override
    public Resource reserveResource(ResourceFactory factory, String baseUrl) {
        String time = String.valueOf(new Date().getTime());
        String index = String.format("%1$4s", counter.incrementAndGet())
                .replace(" ", "0");
        String suffix = time + "-" + index;
        return factory.apply(baseUrl, suffix);
    }

}
