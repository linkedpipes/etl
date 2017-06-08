package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.event.AbstractEvent;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EVENTS;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Default implementation of progress report.
 */
class DefaultProgressReport implements ProgressReport {

    private static final Logger LOG =
            LoggerFactory.getLogger(DefaultProgressReport.class);

    /**
     * Specific implementation or event to report component progress.
     */
    protected static class ReportProgress extends AbstractEvent {

        private final long current;

        private final long total;

        private final String component;

        ReportProgress(long current, long total, String componentUri) {
            super(LP_EVENTS.PROGRESS_REPORT);
            this.current = current;
            this.total = total;
            this.component = componentUri;
            // Create label.
            final StringBuilder message = new StringBuilder(24);
            message.append("Progress ");
            message.append(Long.toString(current));
            message.append(" / ");
            message.append(Long.toString(total));
            this.label = message.toString();
        }

        @Override
        public void write(RdfSource.TripleWriter writer) {
            super.write(writer);
            writer.typed(iri, LP_EVENTS.HAS_TOTAL,
                    Long.toString(total), XSD.LONG);
            writer.typed(iri, LP_EVENTS.HAS_CURRENT,
                    Long.toString(current), XSD.LONG);
            writer.iri(iri, LP_PIPELINE.HAS_COMPONENT, component);
        }

    }

    private long current;

    private long total;

    private long reportStep;

    private long reportNext;

    private final Component.Context context;

    private final String component;

    /**
     * @param context
     * @param component Component IRI.
     */
    public DefaultProgressReport(Component.Context context, String component) {
        this.context = context;
        this.component = component;
    }

    @Override
    public void start(long entriesToProcess) {
        LOG.info("Progress report ({}) ... ", entriesToProcess);
        current = 0;
        total = entriesToProcess;
        reportStep = (long) (total * 0.1f);
        reportNext = reportStep;
        context.sendMessage(new ReportProgress(0, total, component));
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
            context.sendMessage(new ReportProgress(current, total, component));
        }
    }

    @Override
    public void done() {
        LOG.info("Progress report ... done");
        context.sendMessage(new ReportProgress(total, total, component));
    }

}
