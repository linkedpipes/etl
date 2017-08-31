package com.linkedpipes.etl.executor.api.v1.report;

import com.linkedpipes.etl.executor.api.v1.component.task.Task;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;

public interface ReportWriter {

    void onTaskFailed(Task task, Throwable throwable);

    static ReportWriter create(TripleWriter writer) {
        return new DefaultReportWriter(writer);
    }

}
