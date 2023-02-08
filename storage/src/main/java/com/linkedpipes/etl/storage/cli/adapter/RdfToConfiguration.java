package com.linkedpipes.etl.storage.cli.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.model.vocabulary.RDF;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.cli.Configuration;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class RdfToConfiguration {

    private static final String PREFIX =
            "https://etl.linkedpipes.com/ontology/configuration#";

    private static final String TYPE =
            PREFIX + "Configuration";

    private static final String PART =
            PREFIX + "hasPart";

    private static final String STORAGE =
            PREFIX + "Storage";

    private static final String WORKING_DIRECTORY =
            PREFIX + "workingDirectory";

    private static final String PORT =
            PREFIX + "httpPort";

    private static final String OSGI =
            PREFIX + "osgi";

    private static final String PLUGIN_DIRECTORY =
            PREFIX + "pluginDirectory";

    private static final String LOG =
            PREFIX + "log";

    private static final String LOG_DIRECTORY =
            PREFIX + "logDirectory";

    private static final String LOG_LEVEL =
            PREFIX + "logLevel";

    private static final String DOMAIN =
            PREFIX + "domainName";

    private static final String EXECUTOR_MONITOR =
            PREFIX + "executorMonitor";

    private static final String INTERNAL_URL =
            PREFIX + "internalUrl";

    private final StatementsSelector selector;

    private final Configuration next = new Configuration();

    private RdfToConfiguration(StatementsSelector selector) {
        this.selector = selector;
    }

    public static Configuration updateConfiguration(
            Configuration defaults, File file, RDFFormat format)
            throws StorageException {
        Statements statements = Statements.arrayList();
        try {
            statements.file().addAll(file, format);
        } catch (Exception ex) {
            throw new StorageException("Can't load configuration file.", ex);
        }
        StatementsSelector selector = statements.selector();
        RdfToConfiguration loader = new RdfToConfiguration(selector);
        loader.load();
        return defaults.merge(loader.next);
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
                if (types.contains(STORAGE)) {
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
                    next.dataDirectory = value.stringValue();
                    break;
                case OSGI:
                    if (value instanceof Resource resource) {
                        loadOsgi(resource);
                    }
                    break;
                case DOMAIN:
                    next.baseUrl = value.stringValue();
                    break;
                case EXECUTOR_MONITOR:
                    if (value instanceof Resource resource) {
                        loadExecutorMonitor(resource);
                    }
                    break;
                case PORT:
                    if (value instanceof Literal literal) {
                        next.httpPort = literal.intValue();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void loadLog(Resource subject) {
        for (Statement statement : selector.withSubject(subject)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LOG_DIRECTORY:
                    next.logDirectory = value.stringValue();
                    break;
                case LOG_LEVEL:
                    next.logLevel = value.stringValue();
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
                    next.pluginDirectory = value.stringValue();
                    break;
                default:
                    break;
            }
        }
    }

    private void loadExecutorMonitor(Resource subject) {
        for (Statement statement : selector.withSubject(subject)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case INTERNAL_URL:
                    next.executorMonitorUrl = value.stringValue();
                    break;
                default:
                    break;
            }
        }
    }

}
