package com.linkedpipes.etl.executor.monitor.cli.adapter;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.cli.Configuration;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class RdfToConfiguration {

    private static final String PREFIX =
            "https://etl.linkedpipes.com/ontology/configuration#";

    private static final String TYPE =
            PREFIX + "Configuration";

    private static final String PART =
            PREFIX + "hasPart";

    private static final String EXECUTOR_MONITOR =
            PREFIX + "ExecutorMonitor";

    private static final String WORKING_DIRECTORY =
            PREFIX + "workingDirectory";

    private static final String PORT =
            PREFIX + "httpPort";

    private static final String LOG =
            PREFIX + "log";

    private static final String LOG_DIRECTORY =
            PREFIX + "logDirectory";

    private static final String LOG_LEVEL =
            PREFIX + "logLevel";

    private static final String EXECUTOR =
            PREFIX + "executor";

    private static final String INTERNAL_URL =
            PREFIX + "internalUrl";

    private static final String DOMAIN =
            PREFIX + "domainName";

    private static final String DANGLING_RETRY_LIMIT =
            PREFIX + "danglingRetryLimit";

    private static final String HISTORY_COUNT_LIMIT =
            PREFIX + "historyCountLimit";

    private static final String HISTORY_HOUR_LIMIT =
            PREFIX + "historyHourLimit";

    private static final String REPORT =
            PREFIX + "report";

    private static final String SLACK =
            PREFIX + "Slack";

    private static final String FINISHED =
            PREFIX + "finished";

    private static final String FAILED =
            PREFIX + "failed";

    private final StatementsSelector selector;

    private final Configuration next = new Configuration();

    private RdfToConfiguration(StatementsSelector selector) {
        this.selector = selector;
    }

    public static Configuration updateConfiguration(
            Configuration defaults, File file, RDFFormat format)
            throws MonitorException {
        Statements statements = Statements.arrayList();
        try {
            statements.file().addAll(file, format);
        } catch (Exception ex) {
            throw new MonitorException("Can't load configuration file.", ex);
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
                if (types.contains(EXECUTOR_MONITOR)) {
                    loadExecutorMonitor(resource);
                }
            }
        }
    }

    private void loadExecutorMonitor(Resource subject) {
        for (Statement statement : selector.withSubject(subject)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LOG:
                    if (value instanceof Resource resource) {
                        loadLog(resource);
                    }
                    break;
                case REPORT:
                    if (value instanceof Resource resource) {
                        loadReport(resource);
                    }
                    break;
                case DANGLING_RETRY_LIMIT:
                    if (value instanceof Literal literal) {
                        next.danglingRetryLimit = literal.intValue();
                    }
                    break;
                case HISTORY_COUNT_LIMIT:
                    if (value instanceof Literal literal) {
                        next.historyLimit = literal.intValue();
                    }
                    break;
                case HISTORY_HOUR_LIMIT:
                    if (value instanceof Literal literal) {
                        next.historyHourLimit = literal.intValue();
                    }
                    break;
                case WORKING_DIRECTORY:
                    next.dataDirectory = value.stringValue();
                    break;
                case DOMAIN:
                    next.baseUrl = value.stringValue();
                    break;
                case EXECUTOR:
                    if (value instanceof Resource resource) {
                        loadExecutor(resource);
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

    private void loadReport(Resource subject) {
        Collection<Value> types = selector.types(subject);
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI reportType = valueFactory.createIRI(SLACK);
        if (types.contains(reportType)) {
            loadSlack(subject);
        }
    }

    private void loadSlack(Resource subject) {
        for (Statement statement : selector.withSubject(subject)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case FINISHED:
                    next.slackFinishedWebhook = value.stringValue();
                    break;
                case FAILED:
                    next.slackErrorWebhook = value.stringValue();
                    break;
                default:
                    break;
            }
        }
    }

    private void loadExecutor(Resource subject) {
        for (Statement statement : selector.withSubject(subject)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case INTERNAL_URL:
                    next.executorUrl = value.stringValue();
                    break;
                default:
                    break;
            }
        }
    }

}
