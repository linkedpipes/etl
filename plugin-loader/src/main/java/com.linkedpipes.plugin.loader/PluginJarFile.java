package com.linkedpipes.plugin.loader;

import org.eclipse.rdf4j.model.Statement;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;

public class PluginJarFile {

    private final File file;

    private final String jar;

    private final String iri;

    private final List<Statement> definition;

    private final List<Statement> configuration;

    private final List<Statement> configurationDescription;

    private final Map<String, JarEntry> dialogEntries;

    public PluginJarFile(
            File file,
            String jar,
            String iri,
            List<Statement> definition,
            List<Statement> configuration,
            List<Statement> configurationDescription,
            Map<String, JarEntry> dialogEntries) {
        this.file = file;
        this.jar = jar;
        this.iri = iri;
        this.definition = definition;
        this.configuration = configuration;
        this.configurationDescription = configurationDescription;
        this.dialogEntries = dialogEntries;
    }

    public File getFile() {
        return file;
    }

    public String getJar() {
        return jar;
    }

    public String getIri() {
        return iri;
    }

    public List<Statement> getDefinition() {
        return Collections.unmodifiableList(definition);
    }

    public List<Statement> getConfiguration() {
        return Collections.unmodifiableList(configuration);
    }

    public List<Statement> getConfigurationDescription() {
        return Collections.unmodifiableList(configurationDescription);
    }

    public Map<String, JarEntry> getDialogEntries() {
        return Collections.unmodifiableMap(dialogEntries);
    }
}
