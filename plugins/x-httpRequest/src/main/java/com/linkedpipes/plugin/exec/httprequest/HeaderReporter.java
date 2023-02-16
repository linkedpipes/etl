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

    private final IRI responseReportPredicate;

    private final StatementsConsumer consumer;

    private final List<Statement> statements = new ArrayList<>();

    private Integer counter = 0;

    private IRI objectIri;

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
        responseReportPredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_RESPONSE_REPORT);
        this.consumer = consumer;
    }

    public void reportHeaderResponse(
            HttpURLConnection connection, HttpRequestTask task)
            throws LpException {
        prepareForReporting(task);
        connection.getHeaderFields().entrySet().forEach((entry) -> {
            addConnectionToReport();
            reportHeaders(entry.getKey(), entry.getValue());
        });
        consumer.consume(statements);
    }

    private void prepareForReporting(HttpRequestTask task) {
        this.statements.clear();
        this.counter = 0;
        this.objectIri = valueFactory.createIRI(task.deriveIri("response"));
        this.task = task;
    }

    private void addConnectionToReport() {
        statements.add(valueFactory.createStatement(
                valueFactory.createIRI(task.deriveIri("report")),
                responseReportPredicate,
                objectIri));
    }

    private void reportHeaders(String header, List<String> values) {
        if (header == null) {
            reportAsResponseLine(values);
        } else {
            reportAsHeader(header, values);
        }
    }

    private void reportAsResponseLine(List<String> values) {
        for (String value : values) {
            statements.add(valueFactory.createStatement(
                    objectIri, responseLinePredicate,
                    valueFactory.createLiteral(value)));
        }
    }

    private void reportAsHeader(String header, List<String> values) {
        IRI headerIri = createHeaderIri();
        statements.add(valueFactory.createStatement(
                objectIri, headerObjectPredicate, headerIri));
        statements.add(valueFactory.createStatement(
                headerIri, namePredicate, valueFactory.createLiteral(header)));
        for (String value : values) {
            statements.add(valueFactory.createStatement(
                    headerIri, valuePredicate,
                    valueFactory.createLiteral(value)));
        }
    }

    private IRI createHeaderIri() {
        return valueFactory.createIRI(task.deriveIri("header/") + ++counter);
    }

}
