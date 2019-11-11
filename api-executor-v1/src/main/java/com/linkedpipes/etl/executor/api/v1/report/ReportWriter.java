package com.linkedpipes.etl.executor.api.v1.report;

import com.linkedpipes.etl.executor.api.v1.component.task.Task;
import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;

import java.util.Date;

public interface ReportWriter {

    void onTaskFinished(Task task, Date start, Date end);

    void onTaskFailed(Task task, Date start, Date end, Throwable throwable);

    void onTaskFinishedInPreviousRun(Task task);

    String getIriForReport(Task task);

    static ReportWriter create(TripleWriter writer) {
        return new DefaultReportWriter(writer);
    }

}
