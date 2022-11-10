package com.linkedpipes.etl.library.template.plugin.adapter;

import com.linkedpipes.etl.library.template.plugin.PluginException;
import com.linkedpipes.etl.library.template.plugin.model.JavaPluginDefinition;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.model.IRI;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;

public class JavaFileToJavaPlugin {

    private final PluginJarFile jarFile;

    private final List<PluginTemplate> templates = new ArrayList<>();

    private IRI pluginResource;

    protected JavaFileToJavaPlugin(PluginJarFile jarFile) {
        this.jarFile = jarFile;
    }

    public static JavaPlugin asJavaPlugins(File file)
            throws PluginException {
        PluginJarFile jarFile = PluginJarFile.loadFromFile(file);
        JavaFileToJavaPlugin loader = new JavaFileToJavaPlugin(jarFile);
        loader.load();
        return new JavaPlugin(
                loader.pluginResource, file, jarFile.jarFile(),
                jarFile.entries(), loader.templates);
    }

    private void load() throws PluginException {
        JarEntry v1Definition = jarFile.selectByPrefix("jar/definition.");
        JarEntry v2Definition = jarFile.selectByPrefix("definition.");
        if (v1Definition != null) {
            loadPluginTemplateV1(v1Definition);
        } else if (v2Definition != null) {
            loadPluginTemplateV2(v2Definition);
        } else {
            throw new PluginException(
                    "Missing definition in '{}'.", jarFile.fileName());
        }
    }

    private void loadPluginTemplateV1(JarEntry entry) throws PluginException {
        Statements statements = jarFile.loadAsStatement(entry);
        List<JavaPluginDefinition> definitions =
                RdfToJavaPluginDefinition.asJarPluginDefinitions(
                        statements.selector());
        if (definitions.size() != 1) {
            throw new PluginException("Invalid number of definitions.");
        }
        JavaPluginDefinition definition = definitions.get(0);
        pluginResource = definition.plugin();
        templates.add(createPluginTemplateV1());
    }

    private PluginTemplate createPluginTemplateV1()
            throws PluginException {
        Statements definition = jarFile.loadAsStatement(
                jarFile.selectByPrefix("template/definition."));
        Statements configuration = jarFile.loadAsStatement(
                jarFile.selectByPrefix("template/config."));
        Statements configurationDescription = jarFile.loadAsStatement(
                jarFile.selectByPrefix("template/config-desc."));

        Statements statements = Statements.arrayList();
        statements.addAll(definition);
        statements.addAll(configurationDescription);

        List<PluginTemplate> templates = RdfToPluginTemplate.asPluginTemplates(
                statements.selector());
        if (templates.size() != 1) {
            throw new PluginException(
                    "Invalid number of plugins ({}) detected for '{}'.",
                    templates.size(), jarFile.fileName());
        }
        PluginTemplate template = templates.get(0);

        return new PluginTemplate(
                template.resource(),
                template.label(),
                template.color(),
                template.type(),
                template.supportControl(),
                template.tags(),
                template.documentation(),
                prepareDialogEntries("template"),
                template.ports(),
                configuration.withoutGraph(),
                PluginTemplate.defaultConfigurationGraph(template.resource()),
                template.configurationDescription(),
                PluginTemplate.defaultConfigurationDescriptionGraph(
                        template.resource()));
    }

    private Map<String, Map<String, String>> prepareDialogEntries(
            String directory) {
        Map<String, Map<String, String>> result = new HashMap<>();
        String prefix = directory + "/dialog/";
        for (String item : jarFile.selectNamesByPrefix(prefix)) {
            String itemWithoutPrefix = item.substring(prefix.length());
            String dialogName = itemWithoutPrefix.substring(
                    0, itemWithoutPrefix.indexOf("/"));
            Map<String, String> dialogMap =
                    result.computeIfAbsent(dialogName, key -> new HashMap<>());
            String dialogFile = itemWithoutPrefix.substring(
                    itemWithoutPrefix.indexOf("/") + 1);
            dialogMap.put(dialogFile, item);
        }
        return result;
    }

    private void loadPluginTemplateV2(JarEntry entry) throws PluginException {
        Statements statements = jarFile.loadAsStatement(entry);
        List<JavaPluginDefinition> definitions =
                RdfToJavaPluginDefinition.asJarPluginDefinitions(
                        statements.selector());
        if (definitions.size() != 1) {
            throw new PluginException("Invalid number of definitions.");
        }
        JavaPluginDefinition definition = definitions.get(0);
        pluginResource = definition.plugin();
        for (String directory : definition.directories()) {
            templates.add(createPluginTemplateV2(directory));
        }
    }

    private PluginTemplate createPluginTemplateV2(String directory)
            throws PluginException {
        Statements definition = jarFile.loadAsStatement(
                jarFile.selectByPrefix(
                        directory + "/definition."));
        Statements configurationDescription = jarFile.loadAsStatement(
                jarFile.selectByPrefix(
                        directory + "/configuration-description."));

        Statements statements = Statements.arrayList();
        statements.addAll(definition);
        statements.addAll(configurationDescription);

        List<PluginTemplate> templates = RdfToPluginTemplate.asPluginTemplates(
                statements.selector());
        if (templates.size() != 1) {
            throw new PluginException(
                    "Invalid number of plugins ({}) detected for '{}'.",
                    templates.size(), jarFile.fileName());
        }

        Statements configuration = jarFile.loadAsStatement(
                jarFile.selectByPrefix(
                        directory + "/configuration."));

        PluginTemplate template = templates.get(0);
        return new PluginTemplate(
                template.resource(),
                template.label(),
                template.color(),
                template.type(),
                template.supportControl(),
                template.tags(),
                template.documentation(),
                prepareDialogEntries(directory),
                template.ports(),
                configuration.withoutGraph(),
                PluginTemplate.defaultConfigurationGraph(template.resource()),
                template.configurationDescription(),
                PluginTemplate.defaultConfigurationDescriptionGraph(
                        template.resource()));
    }


}
