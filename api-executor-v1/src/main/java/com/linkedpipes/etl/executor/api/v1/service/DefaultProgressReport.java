package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.event.AbstractEvent;
import com.linkedpipes.etl.executor.api.v1.rdf.model.TripleWriter;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP;
import com.linkedpipes.etl.executor.api.v1.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

class DefaultProgressReport implements ProgressReport {

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultProgressReport.class);

    private static final int EXPECTED_LABEL_LEN = 24;

    private static final float REPORT_STEP_SIZE = 0.1f;

    /**
     * Specific implementation or event to report component progress.
     */
    protected static class ReportProgress extends AbstractEvent {

        private final long current;

        private final long total;

        private final String component;

        ReportProgress(long current, long total, String componentUri) {
            super(LP.PROGRESS_REPORT);
            this.current = current;
            this.total = total;
            this.component = componentUri;
            // TODO REFACTORING Extract as a function.
            StringBuilder message = new StringBuilder(EXPECTED_LABEL_LEN);
            message.append("Progress ");
            message.append(Long.toString(current));
            message.append(" / ");
            message.append(Long.toString(total));
            this.label = message.toString();
        }

        @Override
        public void write(TripleWriter writer) {
            super.write(writer);
            writer.typed(iri, LP.HAS_TOTAL, Long.toString(total), XSD.LONG);
            writer.typed(iri, LP.HAS_CURRENT, Long.toString(current), XSD.LONG);
            writer.iri(iri, LP.HAS_COMPONENT, component);
        }

    }

    private int current = 0;

    private long total;

    private long reportStep;

    private long reportNext;

    private final Component.Context context;

    private final String component;

    private final Object lock = new Object();

    /**
     * Constructor.
     *
     * @paran context Component execution context.
     * @param component Component IRI.
     */
    public DefaultProgressReport(Component.Context context, String component) {
        this.context = context;
        this.component = component;
    }

    @Override
    public void start(long entriesToProcess) {
        LOG.info("Progress report start 0/{}", entriesToProcess);
        current = 0;
        total = entriesToProcess;
        reportStep = (long) (total * REPORT_STEP_SIZE);
        reportNext = reportStep;
        context.sendMessage(new ReportProgress(0, total, component));
    }

    @Override
    public void start(Collection<?> collection) {
        start(collection.size());
    }

    @Override
    public void entryProcessed() {
        synchronized (lock) {
            int value = ++current;
            if (value >= reportNext) {
                reportNext += reportStep;
                sendReportMessage(value);
            }
        }
    }

    private void sendReportMessage(int progress) {
        LOG.info("Progress report {}/{}", current, total);
        context.sendMessage(new ReportProgress(progress, total, component));
    }

    @Override
    public void done() {
        LOG.info("Progress report ALL/{}", current);
        context.sendMessage(new ReportProgress(total, total, component));
    }

}
