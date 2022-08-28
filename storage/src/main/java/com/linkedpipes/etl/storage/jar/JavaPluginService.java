package com.linkedpipes.etl.storage.jar;

import com.linkedpipes.etl.library.template.plugin.PluginException;
import com.linkedpipes.etl.library.template.plugin.adapter.java.JavaFileToJavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.storage.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JavaPluginService {

    private static final Logger LOG = LoggerFactory.getLogger(JavaPluginService.class);

    private final Configuration configuration;

    private final Map<String, JavaPlugin> plugins = new HashMap<>();

    @Autowired
    public JavaPluginService(Configuration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void initialize() {
        LOG.debug("Loading plugins ...");
        File root = configuration.getJarDirectory();
        for (File pluginFile : listPluginFiles(root)) {
            JavaPlugin plugin;
            try {
                plugin = JavaFileToJavaPlugin.asJavaPlugins(pluginFile);
            } catch (PluginException ex) {
                LOG.warn("Can't load plugin.", ex);
                continue;
            }
            plugins.put(plugin.iri().stringValue(), plugin);
        }
        LOG.debug("Loading plugins ... done with: {}", plugins.size());
    }

    private List<File> listPluginFiles(File root) {
        File result = configuration.getJarDirectory();
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
     * Return path to the JAR file.
     */
    public JavaPlugin getJavaPlugin(String iri) {
        return plugins.get(iri);
    }

    /**
     * Return collection of all components.
     */
    public Collection<JavaPlugin> getJavaPlugins() {
        return plugins.values();
    }

}
