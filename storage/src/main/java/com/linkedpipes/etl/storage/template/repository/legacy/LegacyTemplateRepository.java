package com.linkedpipes.etl.storage.template.repository.legacy;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.template.reference.ReferenceTemplateLoader;
import com.linkedpipes.etl.library.template.reference.adapter.RawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.adapter.RdfToRawReferenceTemplate;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * In the old structure we have a directory for mapping and a directory for
 * templates. Each template is represented as a directory with definition,
 * interface, configuration, and dialog files.
 *
 * This class provide read only access to those data.
 */
public class LegacyTemplateRepository {

    private static final Logger LOG =
            LoggerFactory.getLogger(LegacyTemplateRepository.class);

    public static final String NAME = null;

    private static final String MAPPING_GRAPH =
            "http://etl.linkedpipes.com/resources/plugins/mapping";

    private static final String MAPPING_FILE = "mapping.trig";

    private static final String DEFINITION_FILE = "definition.trig";

    private static final String INTERFACE_FILE = "interface.trig";

    private static final String CONFIGURATION_FILE = "configuration.trig";

    private final Set<Resource> plugins;

    private final int version;

    public LegacyTemplateRepository(
            List<PluginTemplate> pluginTemplates, int version) {
        plugins = pluginTemplates.stream()
                .map(PluginTemplate::resource)
                .collect(Collectors.toSet());
        this.version = version;
    }

    public List<ReferenceTemplate> loadReferenceTemplates(
            File templateDirectory, File knowledgeDirectory)
            throws StorageException {
        Map<Resource, Resource> mapping =
                loadLocalToOriginalMapping(knowledgeDirectory);
        List<RawReferenceTemplate> rawReferences =
                loadRawReferenceTemplates(templateDirectory);
        ReferenceTemplateLoader loader = new ReferenceTemplateLoader(
                plugins, Collections.emptyMap());
        loader.loadAndMigrate(rawReferences);
        loader.getContainers().stream()
                .filter(ReferenceTemplateLoader.Container::isFailed)
                .forEach(container -> LOG.error(
                        "Can't load template '{}'.",
                        container.rawTemplate().resource,
                        container.exception()));
        if (loader.hasAnyFailed()) {
            throw new StorageException("Can't migrate templates.");
        }
        List<ReferenceTemplate> references = loader.getTemplates();
        return addMappings(mapping, references);
    }

    private Map<Resource, Resource> loadLocalToOriginalMapping(
            File knowledgeDirectory) throws StorageException {
        File file = new File(knowledgeDirectory, MAPPING_FILE);
        Statements statements = Statements.arrayList();
        try {
            statements.file().addAllIfExists(file);
        } catch (IOException ex) {
            throw new StorageException("Can't read mapping file.", ex);
        }
        Map<Resource, Resource> result = new HashMap<>();
        statements.selector().selectByGraph(MAPPING_GRAPH)
                .selector().select(null, OWL.SAMEAS, null)
                .forEach(statement -> {
                    // Remote to original (same domain)
                    // This may not be remote to main ...
                    Resource original = statement.getSubject();
                    if (!statement.getSubject().isResource()) {
                        return;
                    }
                    Resource local = (Resource) statement.getObject();
                    result.put(local, original);
                });
        return result;
    }

    private List<RawReferenceTemplate> loadRawReferenceTemplates(File directory)
            throws StorageException {
        List<File> directories = listReferenceDirectories(directory);
        List<RawReferenceTemplate> result =
                new ArrayList<>(directories.size());
        for (File file : directories) {
            result.add(loadRawReferenceTemplate(file));
        }
        return result;
    }

    private List<File> listReferenceDirectories(File directory)
            throws StorageException {
        if (!directory.isDirectory()) {
            return Collections.emptyList();
        }
        File[] files = directory.listFiles();
        if (files == null) {
            throw new StorageException("Can't list pipeline directory.");
        }
        return Arrays.stream(files)
                .filter(File::isDirectory)
                .filter(file -> !isPluginTemplateDirectory(file))
                .toList();
    }

    private boolean isPluginTemplateDirectory(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.startsWith("jar-");
    }

    private RawReferenceTemplate loadRawReferenceTemplate(File directory)
            throws StorageException {
        Statements content = Statements.wrap(new HashSet<>());
        try {
            content.file().addAll(new File(directory, DEFINITION_FILE));
            content.file().addAll(new File(directory, INTERFACE_FILE));
            content.file().addAll(new File(directory, CONFIGURATION_FILE));
        } catch (IOException ex) {
            throw new StorageException("Can't load template files.", ex);
        }
        List<RawReferenceTemplate> candidates =
                RdfToRawReferenceTemplate.asRawReferenceTemplates(
                        content.selector());
        if (candidates.size() != 1) {
            throw new StorageException(
                    "Invalid number of templates ({}) in '{}'.",
                    candidates.size(), directory);
        }
        RawReferenceTemplate candidate = candidates.get(0);
        // There is no information about version in data, so we add it here.
        candidate.version = version;
        return candidate;
    }

    private List<ReferenceTemplate> addMappings(
            Map<Resource, Resource> mapping,
            List<ReferenceTemplate> references) {
        List<ReferenceTemplate> result = new ArrayList<>(references.size());
        for (ReferenceTemplate reference : references) {
            if (mapping.containsKey(reference.resource())) {
                result.add(new ReferenceTemplate(
                        reference.resource(), reference.version(),
                        reference.plugin(), reference.plugin(),
                        reference.label(), reference.description(),
                        reference.note(), reference.color(),
                        reference.tags(),
                        // We know there is no knowAs for this version.
                        mapping.get(reference.resource()),
                        reference.configuration(),
                        reference.configurationGraph()));
            } else {
                result.add(reference);
            }
        }
        return result;
    }

}
