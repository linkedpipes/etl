package com.linkedpipes.etl.library.template.plugin.adapter.java;

import com.linkedpipes.etl.library.template.plugin.PluginException;
import com.linkedpipes.etl.library.template.plugin.adapter.rdf.RdfToPluginTemplate;
import com.linkedpipes.etl.library.template.plugin.model.JavaPluginDefinition;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.library.rdf.Statements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaFileToJavaPlugin {

    private JavaFileToJavaPlugin() {
    }

    public static JavaPlugin asJavaPlugins(File file)
            throws PluginException {
        PluginJarFile jarFile = PluginJarFile.loadFromFile(file);
        List<JavaPluginDefinition> definitions = jarFile.loadDefinitions();
        if (definitions.size() != 1) {
            throw new PluginException(
                    "Invalid number of definitions found in '{}'.", file);
        }
        JavaPluginDefinition definition = definitions.get(0);
        List<PluginTemplate> templates = new ArrayList<>();
        for (String directory : definition.directories()) {
            templates.add(createPluginTemplate(jarFile, directory));
        }
        return new JavaPlugin(
                file, jarFile.jarFile(), definition.plugin(),
                jarFile.entries(), templates);
    }

    private static PluginTemplate createPluginTemplate(
            PluginJarFile jarFile, String directory)
            throws PluginException {
        Statements definition = jarFile.loadAsStatement(
                jarFile.selectByPrefix(
                        directory + "/definition."));
        Statements configuration = jarFile.loadAsStatement(
                jarFile.selectByPrefix(
                        directory + "/configuration."));
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
        PluginTemplate template = templates.get(0);
        return new PluginTemplate(
                template.resource(),
                template.label(),
                template.color(),
                template.type(),
                template.supportControl(),
                template.tags(),
                template.documentation(),
                prepareDialogEntries(jarFile, directory),
                template.ports(),
                configuration.withoutGraph(),
                PluginTemplate.defaultConfigurationGraph(template.resource()),
                template.configurationDescription(),
                PluginTemplate.defaultConfigurationDescriptionGraph(
                        template.resource()));
    }

    private static List<String> prepareDialogEntries(
            PluginJarFile jarFile, String directory) {
        String prefix = directory + "/dialog/";
        Set<String> result = jarFile.selectNamesByPrefix(prefix)
                .stream()
                // Remove prefix.
                .map(item -> item.substring(prefix.length()))
                // Select just the directory name.
                .map(item -> item.substring(0, item.indexOf("/")))
                // Collect to set to remove duplicity.
                .collect(Collectors.toSet());
        return new ArrayList<>(result);
    }

}
