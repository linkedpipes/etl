package com.linkedpipes.plugin.loader;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader {

    private static final String LP_ETL_PREFIX = "LP-ETL/";

    private static final String V_1_TEMPLATE_PREFIX = "template";

    private static final String DIALOG_PREFIX = "dialog/";

    private static final String DEFAULT_NAMESPACE = "urn";

    private static final IRI HAS_DIALOG;

    private static final IRI HAS_NAME;

    private static final IRI DIALOG;

    private static final IRI JAR_TEMPLATE;

    private static final IRI JAR;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        HAS_DIALOG = valueFactory.createIRI(
                "http://linkedpipes.com/ontology/dialog");
        HAS_NAME = valueFactory.createIRI(
                "http://linkedpipes.com/ontology/Dialog");
        DIALOG = valueFactory.createIRI(
                "http://linkedpipes.com/ontology/name");
        JAR_TEMPLATE = valueFactory.createIRI(
                "http://linkedpipes.com/ontology/JarTemplate");
        JAR = valueFactory.createIRI(
                "http://etl.linkedpipes.com/ontology/JarFile");
    }

    private static final Logger LOG =
            LoggerFactory.getLogger(PluginLoader.class);

    public List<PluginJarFile> loadReferences(File file)
            throws PluginLoaderException {
        JarFile jarFile = loadJarFile(file);
        Map<String, JarEntry> entries = loadJarEntries(jarFile);
        Resource jar = findJarResource(jarFile, entries);
        if (jar == null) {
            LOG.info("Missing JAR file specification for: {}", file);
            return Collections.emptyList();
        }
        List<PluginJarFile> result = new ArrayList<>();
        result.add(createJarFile(
                file, jarFile, jar, entries, V_1_TEMPLATE_PREFIX));
        return result.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private JarFile loadJarFile(File file) throws PluginLoaderException {
        try {
            return new JarFile(file);
        } catch (IOException ex) {
            throw new PluginLoaderException("Can't read file.", ex);
        }
    }

    private HashMap<String, JarEntry> loadJarEntries(JarFile jar) {
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

    private Resource findJarResource(
            JarFile jarFile, Map<String, JarEntry> entries)
            throws PluginLoaderException {
        JarEntry jarDefinition = selectByPrefix(entries, "jar/definition.");
        List<Statement> statements = readAsStatements(jarFile, jarDefinition);
        for (Statement statement : statements) {
            if (!statement.getPredicate().equals(RDF.TYPE)) {
                continue;
            }
            if (statement.getObject().equals(JAR)) {
                return statement.getSubject();
            }
        }
        return null;
    }

    private PluginJarFile createJarFile(
            File file, JarFile jarFile, Resource jar,
            Map<String, JarEntry> allEntries, String prefix)
            throws PluginLoaderException {
        Map<String, JarEntry> entries =
                filterByPrefix(allEntries, prefix + "/");
        // Load all data.
        List<Statement> definition =
                readAsStatements(
                        jarFile,
                        selectByPrefix(entries, "definition."));
        List<Statement> configuration =
                readAsStatements(
                        jarFile,
                        selectByPrefix(entries, "config."));
        List<Statement> configurationDescription =
                readAsStatements(
                        jarFile,
                        selectByPrefix(entries, "config-desc."));
        // Find plugin.
        Resource plugin = findPlugin(definition);
        if (plugin == null) {
            throw new PluginLoaderException(
                    "Can't find plugin definition for:", prefix);
        }
        // Find dialog entries.
        Map<String, JarEntry> dialogEntries =
                filterByPrefix(entries, DIALOG_PREFIX);
        // Update definition.
        Set<String> dialogs = collectDialogNames(dialogEntries.keySet());
        definition.addAll(createDefinitionForDialogs(plugin, dialogs));
        // Return result.
        return new PluginJarFile(
                file,
                jar.stringValue(),
                plugin.stringValue(),
                definition,
                configuration,
                configurationDescription,
                dialogEntries
        );
    }

    private Map<String, JarEntry> filterByPrefix(
            Map<String, JarEntry> entries, String prefix) {
        int prefixSize = prefix.length();
        return entries.entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(prefixSize),
                        Map.Entry::getValue
                ));
    }

    private JarEntry selectByPrefix(
            Map<String, JarEntry> entries, String prefix) {
        for (Map.Entry<String, JarEntry> entry : entries.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private List<Statement> readAsStatements(JarFile jarFile, JarEntry entry)
            throws PluginLoaderException {
        if (entry == null) {
            return Collections.emptyList();
        }
        try (InputStream stream = jarFile.getInputStream(entry)) {
            return readRdfFile(stream, getFormat(entry.getName()));
        } catch (IOException ex) {
            throw new PluginLoaderException(
                    "Can't RDF file: {}", entry.getName(), ex);
        }
    }

    private RDFFormat getFormat(String fileName)
            throws PluginLoaderException {
        // BACKWARD COMPATIBILITY
        if (fileName.toLowerCase().endsWith(".json")) {
            return RDFFormat.JSONLD;
        }
        //
        return Rio.getParserFormatForFileName(fileName).orElseThrow(
                () -> new PluginLoaderException(
                        "Invalid RDF type for file: {}", fileName));
    }

    private List<Statement> readRdfFile(
            InputStream stream, RDFFormat format)
            throws PluginLoaderException {
        List<Statement> statements = new ArrayList<>();
        try {
            RDFParser reader = Rio.createParser(format,
                    SimpleValueFactory.getInstance());
            StatementCollector collector
                    = new StatementCollector(statements);
            reader.setRDFHandler(collector);
            reader.parse(stream, DEFAULT_NAMESPACE);
        } catch (IOException ex) {
            throw new PluginLoaderException("Can't read RDF stream.", ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                LOG.warn("Can't close stream.", ex);
            }
        }
        return statements;
    }

    private Resource findPlugin(List<Statement> statements) {
        for (Statement statement : statements) {
            if (!statement.getPredicate().equals(RDF.TYPE)) {
                continue;
            }
            if (statement.getSubject() instanceof BNode) {
                continue;
            }
            if (statement.getObject().equals(JAR_TEMPLATE)) {
                return statement.getSubject();
            }
        }
        return null;
    }

    private Set<String> collectDialogNames(Collection<String> entries) {
        return entries.stream()
                .map(name -> name.substring(0, name.indexOf("/")))
                .collect(Collectors.toSet());
    }

    private List<Statement> createDefinitionForDialogs(
            Resource plugin, Collection<String> dialogs) {
        List<Statement> output = new ArrayList<>(dialogs.size());
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        for (String dialogName : dialogs) {
            IRI dialog = valueFactory.createIRI(
                    plugin.stringValue(), "/dialog/" + dialogName);
            output.add(valueFactory.createStatement(
                    plugin, HAS_DIALOG, dialog));
            output.add(valueFactory.createStatement(
                    dialog, RDF.TYPE, DIALOG));
            output.add(valueFactory.createStatement(
                    dialog, HAS_NAME, valueFactory.createLiteral(dialogName)));
        }
        return output;
    }

    public void copyFile(
            PluginJarFile plugin, JarEntry entry, File destination)
            throws PluginLoaderException {
        JarFile jarFile = loadJarFile(plugin.getFile());
        try (InputStream stream = jarFile.getInputStream(entry)) {
            FileUtils.copyInputStreamToFile(stream, destination);
        } catch (IOException ex) {
            throw new PluginLoaderException(
                    "Can't copy entry: {}", entry.getName());
        }
    }

}
