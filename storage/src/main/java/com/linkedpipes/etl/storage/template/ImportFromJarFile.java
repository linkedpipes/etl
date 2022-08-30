package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.plugin.PluginException;
import com.linkedpipes.etl.library.template.plugin.PluginTemplateFacade;
import com.linkedpipes.etl.library.template.plugin.adapter.rdf.PluginTemplateToRdf;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.repository.RepositoryReference;
import com.linkedpipes.etl.storage.template.repository.WritableTemplateRepository;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarEntry;

/**
 * Copy template definition from a JAR file to a repository. This class
 * is not thread save.
 */
class ImportFromJarFile {

    private static final Logger LOG =
            LoggerFactory.getLogger(ImportFromJarFile.class);

    private final WritableTemplateRepository repository;

    public ImportFromJarFile(WritableTemplateRepository repository) {
        this.repository = repository;
    }

    public void importJavaPlugin(JavaPlugin plugin) {
        for (PluginTemplate template : plugin.templates()) {
            String iri = template.resource().stringValue();
            File directory = getDirectory(iri);
            directory.mkdirs();
            try {
                copyDialogFiles(plugin, template, directory);
                //
                repository.setConfig(
                        createReference(iri),
                        PluginTemplateToRdf.configurationAsRdf(
                                template));
                repository.setConfigDescription(
                        createReference(iri),
                        PluginTemplateToRdf.configurationDescriptionAsRdf(
                                template));
                // We employ the same for definition and interface.
                repository.setInterface(
                        createReference(iri),
                        PluginTemplateToRdf.definitionAsRdf(template));
                repository.setDefinition(
                        createReference(iri),
                        PluginTemplateToRdf.definitionAsRdf(template));
            } catch (RdfUtils.RdfException | IOException | PluginException ex) {
                LOG.error("Can't import plugin template: {}",
                        template.resource(), ex);
                FileUtils.deleteQuietly(directory);
            }
        }
    }

    private File getDirectory(String id) {
        return repository.getDirectory(createReference(id));
    }

    private RepositoryReference createReference(String iri) {
        return RepositoryReference.createJar(iri);
    }

    private void copyDialogFiles(
            JavaPlugin plugin, PluginTemplate template, File root)
            throws IOException, PluginException {
        root.mkdirs();
        for (var entry : template.dialogs().entrySet()) {
            String directory = "dialog" + File.separator + entry.getKey();
            Map<String, String> files = entry.getValue();
            for (Map.Entry<String, String> fileEntry : files.entrySet()) {
                String fileName = fileEntry.getKey();
                File target = new File(
                        root, directory + File.separator + fileName);
                String key = fileEntry.getValue();
                JarEntry jarEntry = plugin.entry(key);
                byte[] content = PluginTemplateFacade.readFile(
                        plugin, jarEntry);
                FileUtils.writeByteArrayToFile(target, content);
            }
        }
    }

}
