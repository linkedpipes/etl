package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.executor.api.v1.Plugin;

import java.util.Collection;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

/**
 *
 * @author Škoda Petr
 */
class ProgressReportImpl implements ProgressReport {

    private static class ReportProgress extends AbstractEvent {

        private final long current;

        private final long total;

        private final String componentIri;

        ReportProgress(long current, long total, String componentUri) {
            super(LINKEDPIPES.EVENTS.PROGRESS.EVENT_TYPE);
            this.current = current;
            this.total = total;
            this.componentIri = componentUri;
            // Create label.
            StringBuilder message = new StringBuilder(24);
            // If no specific message is created, create a general one.
            message.append("Progress ");
            message.append(Long.toString(current));
            message.append(" / ");
            message.append(Long.toString(total));
            this.label = message.toString();
            this.labelLanguage = "en";
        }

        @Override
        public void serialize(Writer writer) {
            super.serialize(writer);
            writer.add(uri, LINKEDPIPES.EVENTS.PROGRESS.HAS_TOTAL,
                    Long.toString(total),
                    "http://www.w3.org/2001/XMLSchema#long");
            writer.add(uri, LINKEDPIPES.EVENTS.PROGRESS.HAS_CURRENT,
                    Long.toString(current),
                    "http://www.w3.org/2001/XMLSchema#long");
            writer.addUri(uri, LINKEDPIPES.HAS_COMPONENT, componentIri);
        }

    }

    private long current;

    private long total;

    private long reportStep;

    private long reportNext;

    private final Plugin.Context context;

    private final String componentIri;

    ProgressReportImpl(Plugin.Context context, String componentIri) {
        this.context = context;
        this.componentIri = componentIri;
    }

    @Override
    public void start(long entriesToProcess) {
        current = 0;
        total = entriesToProcess;
        reportStep = (long) (total * 0.1f);
        reportNext = reportStep;
        context.sendMessage(new ReportProgress(0, total, componentIri));
    }

    @Override
    public void start(Collection<?> collection) {
        start(collection.size());
    }

    @Override
    public void entryProcessed() {
        ++current;
        if (current >= reportNext) {
            reportNext += reportStep;
            context.sendMessage(
                    new ReportProgress(current, total, componentIri));
        }
    }

    @Override
    public void done() {
        context.sendMessage(new ReportProgress(total, total, componentIri));
    }

}
