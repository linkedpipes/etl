package com.linkedpipes.etl.storage.component.jar;

import com.linkedpipes.etl.storage.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Petr Škoda
 */
@Service
class JarManager {

    private static final Logger LOG
            = LoggerFactory.getLogger(JarManager.class);

    @Autowired
    private Configuration config;

    /**
     * Store JAR components under IRI key.
     */
    private final Map<String, JarComponent> components = new HashMap<>();

    @PostConstruct
    public void initialize() {
        final JarLoader loader = new JarLoader();
        LOG.info("Loading JAR components ...");
        // Make sure the component directory exists.
        final File componentDirectory = config.getJarDirectory();
        if (!componentDirectory.exists()) {
            componentDirectory.mkdirs();
        }
        // Each JAR file has its directory..
        for (File directory : componentDirectory.listFiles()) {
            if (!directory.isDirectory()) {
                continue;
            }
            // Search for JAR files - we assume there will be only
            // one JAR file.
            for (File file : directory.listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(".jar")) {
                    final JarComponent component = loader.load(file);
                    if (component != null) {
                        components.put(component.getIri(), component);
                    }
                }
            }
        }
        LOG.info("Loading JAR components ... done");
    }

    public Map<String, JarComponent> getComponents() {
        return Collections.unmodifiableMap(components);
    }

}
