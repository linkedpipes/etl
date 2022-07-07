package com.linkedpipes.etl.executor.cli.adapter;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.cli.Configuration;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RdfToConfiguration {

    private static final String PREFIX =
            "https://etl.linkedpipes.com/ontology/configuration#";

    private static final String TYPE =
            PREFIX + "Configuration";

    private static final String PART =
            PREFIX + "hasPart";

    private static final String EXECUTOR =
            PREFIX + "Executor";

    private static final String WORKING_DIRECTORY =
            PREFIX + "workingDirectory";

    private static final String PORT =
            PREFIX + "httpPort";

    private static final String OSGI =
            PREFIX + "osgi";

    private static final String PLUGIN_DIRECTORY =
            PREFIX + "pluginDirectory";

    private static final String LIBRARY_DIRECTORY =
            PREFIX + "libraryDirectory";

    private static final String LOG =
            PREFIX + "log";

    private static final String LOG_DIRECTORY =
            PREFIX + "logDirectory";

    private static final String LOG_LEVEL =
            PREFIX + "logLevel";

    private static final String BANNED_PLUGINS =
            PREFIX + "bannedPluginIriPatterns";

    private final StatementsSelector selector;

    private String logDirectory = null;

    private String logLevel = null;

    private String dataDirectory = null;

    private String osgiPlugins = null;

    private String osgiLibraries = null;

    private String osgiWorking = null;

    private Integer httpPort = null;

    private List<String> bannedPluginIriPatterns = new ArrayList<>();

    private RdfToConfiguration(StatementsSelector selector) {
        this.selector = selector;
    }

    public static Configuration updateConfiguration(
            Configuration defaults, File file, RDFFormat format)
            throws ExecutorException {
        Statements statements = Statements.arrayList();
        try {
            statements.file().addAll(file, format);
        } catch (IOException ex) {
            throw new ExecutorException("Can't load configuration file.", ex);
        }
        StatementsSelector selector = statements.selector();
        RdfToConfiguration loader = new RdfToConfiguration(selector);
        loader.load();
        return defaults.merge(
                loader.httpPort,
                loader.dataDirectory,
                loader.logDirectory,
                loader.logLevel,
                loader.osgiWorking,
                loader.osgiLibraries,
                loader.osgiPlugins,
                loader.bannedPluginIriPatterns);
    }

    private void load() {
        for (Resource subject : selector.selectByType(TYPE).subjects()) {
            Collection<Value> parts = selector.select(
                    subject, PART, null).objects();
            for (Value value : parts) {
                if (!value.isResource()) {
                    continue;
                }
                Resource resource = (Resource) value;
                List<String> types = selector.select(
                        resource, RDF.TYPE, null).objects()
                        .stream().map(Value::stringValue).toList();
                if (types.contains(EXECUTOR)) {
                    loadExecutor(resource);
                }
            }
        }
    }

    private void loadExecutor(Resource subject) {
        for (Statement statement : selector.withSubject(subject)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LOG:
                    if (value instanceof Resource resource) {
                        loadLog(resource);
                    }
                    break;
                case WORKING_DIRECTORY:
                    dataDirectory = value.stringValue();
                    break;
                case OSGI:
                    if (value instanceof Resource resource) {
                        loadOsgi(resource);
                    }
                    break;
                case PORT:
                    if (value instanceof Literal literal) {
                        httpPort = literal.intValue();
                    }
                    break;
                default:
                    break;
            }
        }
        this.bannedPluginIriPatterns =
                selector.selectList(subject, BANNED_PLUGINS)
                        .stream().map(Value::stringValue).toList();
    }

    private void loadLog(Resource subject) {
        for (Statement statement : selector.withSubject(subject)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LOG_DIRECTORY:
                    logDirectory = value.stringValue();
                    break;
                case LOG_LEVEL:
                    logLevel = value.stringValue();
                    break;
                default:
                    break;
            }
        }
    }

    private void loadOsgi(Resource subject) {
        for (Statement statement : selector.withSubject(subject)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case PLUGIN_DIRECTORY:
                    osgiPlugins = value.stringValue();
                    break;
                case LIBRARY_DIRECTORY:
                    osgiLibraries = value.stringValue();
                    break;
                case WORKING_DIRECTORY:
                    osgiWorking = value.stringValue();
                    break;
                default:
                    break;
            }
        }
    }

}
