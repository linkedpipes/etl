package com.linkedpipes.etl.executor.api.v1.report;

import com.linkedpipes.etl.executor.api.v1.component.task.Task;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_REPORT;
import com.linkedpipes.etl.rdf.utils.RdfFormatter;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
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
        String reportIri = task.getIri() + "/report";
        writeReportBasic(reportIri, start, end);
        writeTaskReference(reportIri, task);
        writeStatusSuccess(reportIri);
        flushWriter(reportIri, task);
    }

    private void writeReportBasic(String reportIri, Date start, Date end) {
        writer.iri(reportIri, RDF.TYPE, LP_REPORT.REPORT);
        writer.typed(reportIri, LP_REPORT.HAS_START,
                RdfFormatter.toXsdDate(start), XSD.DATETIME);
        writer.typed(reportIri, LP_REPORT.HAS_END,
                RdfFormatter.toXsdDate(end), XSD.DATETIME);
        Long duration = end.getTime() - start.getTime();
        writer.typed(reportIri, LP_REPORT.HAS_DURATION,
                duration.toString(), XSD.LONG);
    }

    private void writeTaskReference(String reportIri, Task task) {
        writer.iri(reportIri, LP_REPORT.HAS_TASK, task.getIri());
    }

    private void writeStatusSuccess(String reportIri) {
        writer.iri(reportIri, LP_REPORT.HAS_STATUS, LP_REPORT.SUCCESS);
    }

    @Override
    public synchronized void onTaskFailed(
            Task task, Date start, Date end, Throwable throwable) {
        LOG.error("Task ({}) failed.", task.getIri(), throwable);
        String reportIri = task.getIri() + "/report";
        writeReportBasic(reportIri, start, end);
        writeTaskReference(reportIri, task);
        writeStatusFailed(reportIri);
        Throwable rootCause = getRootCause(throwable);
        writeError(reportIri, rootCause);
        flushWriter(reportIri, task);
    }

    private void writeStatusFailed(String reportIri) {
        writer.iri(reportIri, LP_REPORT.HAS_STATUS, LP_REPORT.FAILED);
    }

    private Throwable getRootCause(Throwable throwable) {
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable;
    }

    private void writeError(String reportIri, Throwable throwable) {
        String errorIri = reportIri + "/error";
        writer.iri(reportIri, LP_REPORT.HAS_EXCEPTION, errorIri);
        writer.iri(errorIri, RDF.TYPE, LP_REPORT.EXCEPTION);
        String message = throwable.getMessage();
        if (message != null) {
            writer.string(errorIri, LP_REPORT.HAS_MESSAGE,
                    throwable.getMessage(), null);
        }
        writer.string(errorIri, LP_REPORT.HAS_CLASS,
                throwable.getClass().getName(), null);
    }

    private void flushWriter(String reportIri, Task task) {
        try {
            writer.flush();
        } catch (RdfUtilsException exception) {
            LOG.error("Can't flush report.{} for task {}",
                    reportIri, task.getIri());
        }
    }

}
