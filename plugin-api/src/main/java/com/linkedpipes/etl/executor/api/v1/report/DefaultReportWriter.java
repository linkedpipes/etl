package com.linkedpipes.etl.executor.api.v1.report;

import com.linkedpipes.etl.executor.api.v1.component.task.Task;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP;
import com.linkedpipes.etl.executor.api.v1.vocabulary.RDF;
import com.linkedpipes.etl.executor.api.v1.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

class DefaultReportWriter implements ReportWriter {

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultReportWriter.class);

    private final TripleWriter writer;

    public DefaultReportWriter(TripleWriter writer) {
        this.writer = writer;
    }

    @Override
    public synchronized void onTaskFinished(Task task, Date start, Date end) {
        long downloadTime = end.getTime() - start.getTime();
        LOG.info("Task '{}' finished in {} ms", task.getIri(), downloadTime);
        String reportIri = getIriForReport(task);
        writeReportBasic(reportIri, start, end);
        writeTaskReference(reportIri, task);
        writeStatusSuccess(reportIri);
        flushWriter(reportIri, task);
    }

    private void writeReportBasic(String reportIri, Date start, Date end) {
        writer.iri(reportIri, RDF.TYPE, LP.REPORT);
        writer.date(reportIri, LP.HAS_START, start);
        writer.date(reportIri, LP.HAS_END, end);
        String durationAsStr = String.valueOf(end.getTime() - start.getTime());
        writer.typed(reportIri, LP.HAS_DURATION, durationAsStr, XSD.LONG);
    }

    private void writeTaskReference(String reportIri, Task task) {
        writer.iri(reportIri, LP.HAS_TASK, task.getIri());
    }

    private void writeStatusSuccess(String reportIri) {
        writer.iri(reportIri, LP.HAS_STATUS, LP.SUCCESS);
    }

    @Override
    public synchronized void onTaskFailed(
            Task task, Date start, Date end, Throwable throwable) {
        long downloadTime = end.getTime() - start.getTime();
        LOG.error("Task '{}' failed in {} ms",
                task.getIri(), downloadTime, throwable);
        String reportIri = getIriForReport(task);
        writeReportBasic(reportIri, start, end);
        writeTaskReference(reportIri, task);
        writeStatusFailed(reportIri);
        Throwable rootCause = getRootCause(throwable);
        writeError(reportIri, rootCause);
        flushWriter(reportIri, task);
    }

    private void writeStatusFailed(String reportIri) {
        writer.iri(reportIri, LP.HAS_STATUS, LP.FAILED);
    }

    private Throwable getRootCause(Throwable throwable) {
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable;
    }

    private void writeError(String reportIri, Throwable throwable) {
        String errorIri = reportIri + "/error";
        writer.iri(reportIri, LP.HAS_EXCEPTION, errorIri);
        writer.iri(errorIri, RDF.TYPE, LP.EXCEPTION);
        String message = throwable.getMessage();
        if (message != null) {
            writer.string(errorIri, LP.HAS_MESSAGE, throwable.getMessage());
        }
        writer.string(errorIri, LP.HAS_CLASS, throwable.getClass().getName());
    }

    private void flushWriter(String reportIri, Task task) {
        try {
            writer.flush();
        } catch (RdfException exception) {
            LOG.error("Can't flush report.{} for task {}",
                    reportIri, task.getIri());
        }
    }

    public void onTaskFinishedInPreviousRun(Task task) {
        // Ignore tasks executed in previous run.
        LOG.info("Task '{}' finished in previous execution.", task.getIri());
    }

    @Override
    public String getIriForReport(Task task) {
        return task.deriveIri("report");
    }

}
