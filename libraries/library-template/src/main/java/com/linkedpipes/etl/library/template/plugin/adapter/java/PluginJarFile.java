package com.linkedpipes.etl.library.template.plugin.adapter.java;

import com.linkedpipes.etl.library.template.plugin.PluginException;
import com.linkedpipes.etl.library.template.plugin.adapter.rdf.RdfAsJarPluginDefinition;
import com.linkedpipes.etl.library.template.plugin.model.JavaPluginDefinition;
import com.linkedpipes.etl.library.rdf.Statements;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class PluginJarFile {

    private static final String LP_ETL_PREFIX = "LP-ETL/";

    private final File file;

    private JarFile jarFile;

    /**
     * The key is without the LP ETL specific prefix.
     */
    private HashMap<String, JarEntry> entries;

    protected PluginJarFile(File file) {
        this.file = file;
    }

    public static PluginJarFile loadFromFile(File file) throws PluginException {
        PluginJarFile result = new PluginJarFile(file);
        result.load(file);
        return result;
    }

    private void load(File file) throws PluginException {
        jarFile = loadJarFile(file);
        entries = listEntries(jarFile);
    }

    private JarFile loadJarFile(File file) throws PluginException {
        try {
            return new JarFile(file);
        } catch (IOException ex) {
            throw new PluginException("Can't read file.", ex);
        }
    }

    private HashMap<String, JarEntry> listEntries(JarFile jar) {
        HashMap<String, JarEntry> result = new HashMap<>();
        for (var enums = jar.entries(); enums.hasMoreElements(); ) {
            JarEntry entry = enums.nextElement();
            String name = entry.getName();
            boolean isDirectory = name.endsWith("/");
            if (isDirectory || !name.startsWith(LP_ETL_PREFIX)) {
                continue;
            }
            result.put(name.substring(LP_ETL_PREFIX.length()), entry);
        }
        return result;
    }

    public List<JavaPluginDefinition> loadDefinitions() throws PluginException {
        JarEntry entry = selectByPrefix("definition.");
        if (entry == null) {
            return Collections.emptyList();
        }
        Statements statements = loadAsStatement(entry);
        return RdfAsJarPluginDefinition.asJarPluginDefinitions(
                statements.selector());
    }

    public JarEntry selectByPrefix(String prefix) {
        for (Map.Entry<String, JarEntry> entry : entries.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Statements loadAsStatement(JarEntry entry) throws PluginException {
        if (entry == null) {
            return Statements.empty();
        }
        Statements statements = Statements.arrayList();
        String fileName = entry.getName();
        Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
        if (format.isEmpty()) {
            throw new PluginException("Can't detect entry file type.");
        }
        try (InputStream stream = jarFile.getInputStream(entry)) {
            statements.file().addAll(stream, format.get());
        } catch (IOException ex) {
            throw new PluginException(
                    "Can't RDF file: {}", entry.getName(), ex);
        }
        return statements;
    }

    /**
     * Select and return entries with given prefix ignoring the LP ETL
     * specific prefix.
     */
    public List<String> selectNamesByPrefix(String prefix) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, JarEntry> entry : entries.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public String fileName() {
        return file.getName();
    }

    public JarFile jarFile() {
        return jarFile;
    }

    public HashMap<String, JarEntry> entries() {
        return entries;
    }

}
