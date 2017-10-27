package com.linkedpipes.plugin.exec.httprequest;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

class HeaderReporter {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final IRI headerObjectPredicate;

    private final IRI namePredicate;

    private final IRI responseLinePredicate;

    private final IRI valuePredicate;

    private final StatementsConsumer consumer;

    private final List<Statement> statements = new ArrayList<>();

    private Integer counter = 0;

    private IRI taskIri;

    private HttpRequestTask task;

    public HeaderReporter(StatementsConsumer consumer) {
        headerObjectPredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_HEADER_OBJECT);
        namePredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_NAME);
        valuePredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_VALUE);
        responseLinePredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_RESPONSE_LINE);
        this.consumer = consumer;
    }

    public void reportHeaderResponse(
            HttpURLConnection connection, HttpRequestTask task)
            throws LpException  {
        if (!task.isOutputHeaders()) {
            return;
        }
        prepareForReporting(task);
        connection.getHeaderFields().entrySet().forEach((entry) -> {
            reportHeaders(entry.getKey(), entry.getValue());
        });
        consumer.consume(statements);
    }

    private void prepareForReporting(HttpRequestTask task) {
        this.statements.clear();
        this.counter = 0;
        this.taskIri = valueFactory.createIRI(task.getIri());
        this.task = task;
    }

    private void reportHeaders(String header, List<String> values) {
        if (header == null) {
            reportResponseLine(values);
        } else {
            reportHeader(header, values);
        }
    }

    private void reportResponseLine(List<String> values) {
        for (String value : values) {
            statements.add(valueFactory.createStatement(
                    taskIri, responseLinePredicate,
                    valueFactory.createLiteral(value)));
        }
    }

    private void reportHeader(String header, List<String> values) {
        IRI headerIri = createHeaderIri();
        statements.add(valueFactory.createStatement(
                taskIri, headerObjectPredicate, headerIri));
        statements.add(valueFactory.createStatement(
                headerIri, namePredicate, valueFactory.createLiteral(header)));
        for (String value : values) {
            statements.add(valueFactory.createStatement(
                    headerIri, valuePredicate,
                    valueFactory.createLiteral(value)));
        }
    }


    private IRI createHeaderIri() {
        return valueFactory.createIRI(task.getIri() + "/header/" + ++counter);
    }

}
