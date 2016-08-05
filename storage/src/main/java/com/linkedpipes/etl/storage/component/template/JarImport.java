package com.linkedpipes.etl.storage.component.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.component.jar.JarComponent;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Import a component from given JAR file.
 *
 * @author Petr Škoda
 */
final class JarImport {

    private JarImport() {
    }

    /**
     * Holds information about JAR file content.
     */
    private static class JarInfo {

        private final JarFile jar;

        private final JarEntry interfaceEntry;

        private final JarEntry configEntry;

        private final JarEntry configDescEntry;

        private final JarEntry definitionEntry;

        public JarInfo(JarFile jar, JarEntry interfaceEntry,
                JarEntry configInstanceEntry, JarEntry configDescEntry,
                JarEntry definitionEntry) {
            this.jar = jar;
            this.interfaceEntry = interfaceEntry;
            this.configEntry = configInstanceEntry;
            this.configDescEntry = configDescEntry;
            this.definitionEntry = definitionEntry;
        }
    }

    /**
     * Create a component in given directory from given JAR component.
     * Read content from LP-ETL/template JAR directory.
     *
     * @param jarComponent
     * @param destination
     * @throws BaseException
     */
    public static void create(JarComponent jarComponent, File destination)
            throws BaseException {
        // Extract dialog and static content.
        final JarInfo jarInfo = readJarFile(jarComponent.getFile(),
                destination);
        // Load and process interface.
        Collection<Statement> interfaceRdf = readAsRdf(jarInfo,
                jarInfo.interfaceEntry);
        final Resource resource = RdfUtils.find(interfaceRdf,
                FullTemplate.TYPE);
        if (resource == null) {
            throw new BaseException("Missing template resource: {}",
                    jarComponent.getFile());
        }
        interfaceRdf = RdfUtils.forceContext(interfaceRdf, resource);
        // Add information about dialogs into the interface.
        final File dialogRoot = new File(destination, "dialog");
        final ValueFactory vf = SimpleValueFactory.getInstance();
        for (File file : dialogRoot.listFiles()) {
            if (!file.isDirectory()) {
                continue;
            }
            final Resource dialogResource = vf.createIRI(resource.stringValue(),
                    "/dialog/" + file.getName());
            // Reference to a dialog.
            interfaceRdf.add(vf.createStatement(
                    resource,
                    vf.createIRI("http://linkedpipes.com/ontology/dialog"),
                    dialogResource,
                    resource
            ));
            // Dialog information.
            interfaceRdf.add(vf.createStatement(
                    dialogResource,
                    RDF.TYPE,
                    vf.createIRI("http://linkedpipes.com/ontology/Dialog"),
                    resource
            ));
            interfaceRdf.add(vf.createStatement(
                    dialogResource,
                    vf.createIRI("http://linkedpipes.com/ontology/name"),
                    vf.createLiteral(file.getName()),
                    resource
            ));
        }
        RdfUtils.write(new File(destination, Template.INTERFACE_FILE),
                RDFFormat.TRIG, interfaceRdf);
        // Save RDF configurations.
        final Resource configIri = SimpleValueFactory.getInstance().createIRI(
                resource.stringValue() + "/configuration");
        RdfUtils.write(new File(destination, Template.CONFIG_FILE),
                RDFFormat.TRIG, readAsRdf(jarInfo, jarInfo.configEntry,
                        configIri));
        RdfUtils.write(new File(destination, Template.CONFIG_DESC_FILE),
                RDFFormat.TRIG, readAsRdf(jarInfo, jarInfo.configDescEntry,
                        configIri));
        // Update definition.
        final Collection<Statement> definitionRdf = readAsRdf(jarInfo,
                jarInfo.definitionEntry, resource);
        // As we generate the configuration IRI for configuration we
        // need to add a reference.
        definitionRdf.add(vf.createStatement(resource,
                vf.createIRI(
                        "http://linkedpipes.com/ontology/configurationGraph"),
                configIri, resource
        ));
        RdfUtils.write(new File(destination, Template.DEFINITION_FILE),
                RDFFormat.TRIG, definitionRdf);
    }

    /**
     * Read and return content of given JAR entry as RDF.
     *
     * @param jar
     * @param entry
     * @return
     * @throws BaseException
     */
    private static Collection<Statement> readAsRdf(JarInfo jar, JarEntry entry)
            throws BaseException {
        try (InputStream stream = jar.jar.getInputStream(entry)) {
            return RdfUtils.read(stream,
                    RdfUtils.getFormat(new File(entry.getName())));
        } catch (IOException ex) {
            throw new BaseException("Can't load definition: {}",
                    entry.getName());
        }
    }

    /**
     * Read and return content of given JAR entry as RDF with given context.
     *
     * @param jar
     * @param entry
     * @param graph
     * @return
     * @throws BaseException
     */
    private static Collection<Statement> readAsRdf(JarInfo jar, JarEntry entry,
            Resource graph) throws BaseException {
        try (InputStream stream = jar.jar.getInputStream(entry)) {
            Collection<Statement> statements = RdfUtils.read(stream,
                    RdfUtils.getFormat(new File(entry.getName())));
            return RdfUtils.forceContext(statements, graph);
        } catch (IOException ex) {
            throw new BaseException("Can't load definition: {}",
                    entry.getName());
        }
    }

    /**
     * Copy the dialog and static resources to destination and return class
     * with references to entries to read.
     *
     * @param file
     * @param destination
     * @return
     */
    private static JarInfo readJarFile(File file, File destination)
            throws BaseException {
        final JarFile jar;
        JarEntry interfaceEntry = null;
        JarEntry configEntry = null;
        JarEntry configDescEntry = null;
        JarEntry definitionEntry = null;
        try {
            jar = new JarFile(file);
            for (Enumeration<JarEntry> enums = jar.entries();
                    enums.hasMoreElements(); ) {
                final JarEntry entry = enums.nextElement();
                final String name = entry.getName();
                // Skip directories.
                if (name.endsWith("/")) {
                    continue;
                }
                if (name.startsWith("LP-ETL/template/dialog") ||
                        name.startsWith("LP-ETL/template/static")) {
                    // Use name without the LP-ETL/template prefix
                    final String targetName = name.substring(15);
                    // Copy.
                    final File targetFile = new File(destination, targetName);
                    targetFile.getParentFile().mkdirs();
                    try (InputStream stream = jar.getInputStream(entry)) {
                        FileUtils.copyInputStreamToFile(stream, targetFile);
                    }
                } else if (name.startsWith("LP-ETL/template/interface.")) {
                    interfaceEntry = entry;
                } else if (name.startsWith("LP-ETL/template/definition.")) {
                    definitionEntry = entry;
                } else if (name.startsWith("LP-ETL/template/config-desc.")) {
                    configDescEntry = entry;
                } else if (name.startsWith("LP-ETL/template/config.")) {
                    configEntry = entry;
                }
            }
        } catch (IOException ex) {
            throw new BaseException("Can't read template: {}", file);
        }
        // Check presence of definition and configuration.
        if (interfaceEntry == null || configEntry == null ||
                configDescEntry == null || definitionEntry == null) {
            throw new BaseException("Incomplete template definition: {}", file);
        }
        return new JarInfo(jar, interfaceEntry, configEntry,
                configDescEntry, definitionEntry);
    }

}
