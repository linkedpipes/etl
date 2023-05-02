package com.linkedpipes.plugin.http.request.main;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeaderWriter {

    private static final IRI headerObjectPredicate;

    private static final IRI namePredicate;

    private static final IRI responseLinePredicate;

    private static final IRI valuePredicate;

    private static final IRI reportResponsePredicate;

    static {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        headerObjectPredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_HEADER_OBJECT);
        namePredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_NAME);
        valuePredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_VALUE);
        responseLinePredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_RESPONSE_LINE);
        reportResponsePredicate = valueFactory.createIRI(
                HttpRequestVocabulary.HAS_RESPONSE_REPORT);
    }

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final List<Statement> buffer = new ArrayList<>();

    private final StatementsConsumer writer;

    private final HttpRequestTask task;

    private final Resource reportResource;

    private final Resource responseResource;

    private int counter = 0;

    public HeaderWriter(
            StatementsConsumer writer,
            HttpRequestTask task,
            Resource reportResource) {
        this.writer = writer;
        this.task = task;
        this.reportResource = reportResource;
        this.responseResource = createResponseResource();
    }

    private IRI createResponseResource() {
        return valueFactory.createIRI(task.deriveIri("response"));
    }

    public void write(HttpResponse response) throws LpException {
        addConnectionToReport();
        reportResponseLine(response.getStatusLine());
        Arrays.stream(response.getAllHeaders()).unordered().forEach(header -> {
            reportHeader(header.getName(), header.getValue());
        });
        writer.write(buffer);
    }

    private void addConnectionToReport() {
        buffer.add(valueFactory.createStatement(
                reportResource, reportResponsePredicate, responseResource));
    }

    private void reportResponseLine(StatusLine statusLine) {
        buffer.add(valueFactory.createStatement(
                responseResource,
                responseLinePredicate,
                valueFactory.createLiteral(statusLine.getReasonPhrase())));
    }

    private void reportHeader(String header, String value) {
        Resource headerResource = createHeaderResource();
        buffer.add(valueFactory.createStatement(
                responseResource, headerObjectPredicate,
                headerResource));
        buffer.add(valueFactory.createStatement(
                headerResource, namePredicate,
                valueFactory.createLiteral(header)));
        buffer.add(valueFactory.createStatement(
                headerResource, valuePredicate,
                valueFactory.createLiteral(value)));
    }

    private IRI createHeaderResource() {
        return valueFactory.createIRI(task.deriveIri("header/") + ++counter);
    }

}
