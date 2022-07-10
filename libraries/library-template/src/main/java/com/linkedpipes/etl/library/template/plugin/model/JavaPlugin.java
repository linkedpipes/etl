package com.linkedpipes.etl.library.template.plugin.model;

import org.eclipse.rdf4j.model.IRI;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Single JAR file can host multiple plugins.
 */
public record JavaPlugin(
        /*
         * Path to plugin file.
         */
        File file,
        /*
         * Reference to loaded Java file.
         */
        JarFile jarFile,
        /*
         * Plugin identification.
         */
        IRI plugin,
        /*
         * List of all detected file entries with LP ETL specific prefix
         * removed.
         */
        Map<String, JarEntry> resources,
        /*
         * List of templates detected in the plugin.
         */
        List<PluginTemplate> templates
) {

}
