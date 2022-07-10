package com.linkedpipes.etl.storage.jar;

import com.linkedpipes.etl.storage.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
class JarService {

    private static final Logger LOG
            = LoggerFactory.getLogger(JarService.class);

    private final Configuration config;

    @Autowired
    public JarService(Configuration config) {
        this.config = config;
    }

    /**
     * Store JAR components under IRI key.
     */
    private final Map<String, JarComponent> components = new HashMap<>();

    @PostConstruct
    public void initialize() {
        JarLoader loader = new JarLoader();
        LOG.info("Loading JAR components ...");
        // Make sure the component directory exists.
        File componentDirectory = config.getJarDirectory();
        if (!componentDirectory.exists()) {
            componentDirectory.mkdirs();
        }
        FileUtils.listFiles(componentDirectory, new String[]{"jar"}, true
        ).forEach((file) -> {
            if (!file.isDirectory() && file.getName().endsWith(".jar")) {
                JarComponent component = loader.load(file);
                if (component != null) {
                    components.put(component.getIri(), component);
                }
            }
        });
        LOG.info("Loading JAR components ... done");
    }

    public Map<String, JarComponent> getComponents() {
        return Collections.unmodifiableMap(components);
    }

}
