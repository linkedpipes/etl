package com.linkedpipes.etl.storage.plugin;

import com.linkedpipes.etl.library.template.plugin.PluginException;
import com.linkedpipes.etl.library.template.plugin.adapter.JavaFileToJavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.Configuration;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaPluginService {

    private static final Logger LOG =
            LoggerFactory.getLogger(JavaPluginService.class);

    private final Configuration configuration;

    private final Map<String, JavaPlugin> plugins = new HashMap<>();

    private final Map<Resource, JavaPlugin> pluginTemplateToPlugin =
            new HashMap<>();

    public JavaPluginService(Configuration configuration) {
        this.configuration = configuration;
    }

    public void initialize() {
        LOG.debug("Loading plugins ...");
        File root = configuration.getJavaPluginsDirectory();
        for (File pluginFile : listPluginFiles(root)) {
            JavaPlugin plugin;
            try {
                plugin = JavaFileToJavaPlugin.asJavaPlugins(pluginFile);
            } catch (PluginException ex) {
                LOG.warn("Can't load plugin.", ex);
                continue;
            }
            plugins.put(plugin.iri().stringValue(), plugin);
            for (PluginTemplate template : plugin.templates()) {
                pluginTemplateToPlugin.put(template.resource(), plugin);
            }
        }
        LOG.info("Loading plugins ... done with {} plugins", plugins.size());
    }

    private List<File> listPluginFiles(File root) {
        File result = configuration.getJavaPluginsDirectory();
        if (!result.exists()) {
            return Collections.emptyList();
        }
        String[] extensions = new String[]{"jar"};
        return FileUtils.listFiles(root, extensions, true).stream()
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().endsWith(".jar"))
                .toList();
    }

    /**
     * Return collection of all components.
     */
    public Collection<JavaPlugin> getJavaPlugins() {
        return plugins.values();
    }

    public JavaPlugin getPluginForPluginTemplate(Resource resource) {
        return pluginTemplateToPlugin.get(resource);
    }

}
