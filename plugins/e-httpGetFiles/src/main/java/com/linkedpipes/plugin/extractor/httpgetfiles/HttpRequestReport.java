package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class HttpRequestReport {

    private static final Logger LOG =
            LoggerFactory.getLogger(HttpRequestReport.class);

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final IRI headerObjectPredicate;

    private final IRI namePredicate;

    private final IRI responseLinePredicate;

    private final IRI valuePredicate;

    private final IRI responseReportPredicate;

    private final IRI errorPredicate;

    private final StatementsConsumer consumer;

    private final List<Statement> statements = new ArrayList<>();

    private Integer counter = 0;

    private IRI objectIri;

    private DownloadTask task;

    private final ReportWriter reportWriter;

    public HttpRequestReport(
            StatementsConsumer consumer, ReportWriter reportWriter) {
        headerObjectPredicate = valueFactory.createIRI(
                HttpGetFilesVocabulary.HAS_HEADER_OBJECT);
        namePredicate = valueFactory.createIRI(
                HttpGetFilesVocabulary.HAS_NAME);
        valuePredicate = valueFactory.createIRI(
                HttpGetFilesVocabulary.HAS_VALUE);
        responseLinePredicate = valueFactory.createIRI(
                HttpGetFilesVocabulary.HAS_RESPONSE_LINE);
        responseReportPredicate = valueFactory.createIRI(
                HttpGetFilesVocabulary.HAS_RESPONSE_REPORT);
        errorPredicate = valueFactory.createIRI(
                HttpGetFilesVocabulary.HAS_ERROR_MESSAGE);
        this.consumer = consumer;
        this.reportWriter = reportWriter;
    }

    public void setTask(DownloadTask task) {
        this.task = task;
    }

    public void reportHeaderResponse(HttpURLConnection connection)
            throws LpException {
        prepareForReporting(task);
        reportErrorLine(connection);
        reportResponseCode(connection);
        connection.getHeaderFields().entrySet().forEach((entry) -> {
            addConnectionToReport();
            reportHeaders(entry.getKey(), entry.getValue());
        });
        consumer.consume(statements);
    }

    private void prepareForReporting(DownloadTask task) {
        this.statements.clear();
        this.counter = 0;
        this.objectIri = valueFactory.createIRI(task.getIri() + "/response");
        this.task = task;
    }

    private void reportErrorLine(HttpURLConnection connection) {
        String errorMessage;
        InputStream errStream = connection.getErrorStream();
        if (errStream != null) {
            try {
                errorMessage = IOUtils.toString(errStream, "UTF-8");
            } catch (Throwable ex) {
                return;
            }
            LOG.debug("Error stream: {}", errorMessage);
            statements.add(valueFactory.createStatement(
                    valueFactory.createIRI(reportWriter.getIriForReport(task)),
                    errorPredicate,
                    valueFactory.createLiteral(errorMessage)));
        }
    }

    private void reportResponseCode(HttpURLConnection connection) {
        try {
            int responseCode = connection.getResponseCode();
            LOG.debug(" response code: {}", responseCode);
            statements.add(valueFactory.createStatement(
                    valueFactory.createIRI(reportWriter.getIriForReport(task)),
                    errorPredicate,
                    valueFactory.createLiteral(responseCode)));
        } catch (IOException ex) {
            LOG.warn("Can't read status code.");
        }
    }

    private void addConnectionToReport() {
        statements.add(valueFactory.createStatement(
                valueFactory.createIRI(reportWriter.getIriForReport(task)),
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
            LOG.debug(" header: {} : {}", header, value);
        }
    }

    private IRI createHeaderIri() {
        return valueFactory.createIRI(
                reportWriter.getIriForReport(task) + "/header/" + ++counter);
    }

}
