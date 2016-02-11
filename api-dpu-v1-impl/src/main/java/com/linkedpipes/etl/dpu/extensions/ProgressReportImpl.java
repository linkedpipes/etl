package com.linkedpipes.etl.dpu.extensions;

import com.linkedpipes.etl.dpu.api.extensions.ProgressReport;

import java.util.Collection;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.context.Context;
import com.linkedpipes.etl.executor.api.v1.event.ComponentProgress;
import com.linkedpipes.utils.core.entity.boundary.EntityLoader;
import com.linkedpipes.utils.core.event.boundary.AbstractEvent;

/**
 *
 * @author Škoda Petr
 */
public class ProgressReportImpl implements ProgressReport, ManageableExtension {

    private static class Configuration implements EntityLoader.Loadable {

        private float reportPercentage = 0.1f;

        /**
         * Is used it the total number of entries is unknown.
         *
         * TODO: Load from configuration
         */
        private Integer reportStep = null;

        @Override
        public EntityLoader.Loadable load(String predicate, String value) throws EntityLoader.LoadingFailed {
            switch (predicate) {
                case LINKEDPIPES.EVENTS.PROGRESS.HAS_STEP_REPORT_SIZE:
                    try {
                        reportPercentage = Float.parseFloat(value);
                    } catch (NumberFormatException ex) {
                        throw new EntityLoader.LoadingFailed("Inbvalid float value:" + value, ex);
                    }
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public void validate() throws EntityLoader.LoadingFailed {
            if (reportPercentage <= 0 || reportPercentage >= 1) {
                throw new EntityLoader.LoadingFailed(String.format("Invalid report step size: %f",
                        reportPercentage));
            }
        }

    }

    private static class ReportProgress extends AbstractEvent implements ComponentProgress {

        private final Integer current;

        private final Integer total;

        private final String componentUri;

        private final String optionalType;

        public ReportProgress(Integer current, Integer total, String componentUri, String optionalType) {
            super(LINKEDPIPES.EVENTS.PROGRESS.EVENT_TYPE);
            this.current = current;
            this.total = total;
            this.componentUri = componentUri;
            this.optionalType = optionalType;
            // Create label.
            StringBuilder message = null;
            if (optionalType != null) {
                message = new StringBuilder(10);
                switch (optionalType) {
                    case LINKEDPIPES.EVENTS.PROGRESS.EVENT_START:
                        message.append("Start");
                        break;
                    case LINKEDPIPES.EVENTS.PROGRESS.EVENT_DONE:
                        message.append("Done");
                        break;
                    default:
                        break;
                }
            }
            // If no specific message is created, create a general one.
            if (message == null) {
                message = new StringBuilder(24);
                if (LINKEDPIPES.EVENTS.PROGRESS.EVENT_DONE.equals(optionalType)) {
                    message.append("Done.");
                } else {
                    message.append("Progress ");
                    if (current == null) {
                        message.append("?");
                    } else {
                        message.append(Long.toString(current));
                    }
                    message.append(" / ");
                    if (total == null) {
                        message.append("?");
                    } else {
                        message.append(Long.toString(total));
                    }
                }
            }
            this.label = message.toString();
            this.labelLanguage = "en";
        }

        @Override
        public void write(StatementWriter writer) {
            super.write(writer);
            if (total != null) {
                writer.add(uri, LINKEDPIPES.EVENTS.PROGRESS.HAS_TOTAL, Long.toString(total),
                        "http://www.w3.org/2001/XMLSchema#long");
            }
            if (current != null) {
                writer.add(uri, LINKEDPIPES.EVENTS.PROGRESS.HAS_CURRENT, Long.toString(current),
                        "http://www.w3.org/2001/XMLSchema#long");
            }
            writer.addUri(uri, LINKEDPIPES.HAS_COMPONENT, componentUri);
            if (optionalType != null) {
                writer.addUri(uri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", optionalType);
            }
        }

        @Override
        public Integer getTotal() {
            return total;
        }

        @Override
        public Integer getCurrent() {
            return current;
        }

        @Override
        public String getComponentUri() {
            return componentUri;
        }

    }

    private int current;

    private Integer total;

    private int reportStep;

    private int reportNext;

    private final Configuration configuration = new Configuration();

    private String componentUri;

    private final Context context;

    public ProgressReportImpl(Context context) {
        this.context = context;
    }

    @Override
    public void startTotalUnknown(int reportStepSuggestion) {
        current = 0;
        total = null;
        if (configuration.reportStep == null) {
            reportStep = reportStepSuggestion;
        } else {
            reportStep = reportStepSuggestion;
        }
        reportNext = reportStep;
        context.sendEvent(new ReportProgress(0, null, componentUri, LINKEDPIPES.EVENTS.PROGRESS.EVENT_START));
    }

    @Override
    public void start(int entriesToProcess) {
        current = 0;
        total = entriesToProcess;
        reportStep = (int) (total * configuration.reportPercentage);
        reportNext = reportStep;
        context.sendEvent(new ReportProgress(0, total, componentUri, LINKEDPIPES.EVENTS.PROGRESS.EVENT_START));
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
            context.sendEvent(new ReportProgress(current, total, componentUri, null));
        }
    }

    @Override
    public void done() {
        context.sendEvent(new ReportProgress(null, null, componentUri, LINKEDPIPES.EVENTS.PROGRESS.EVENT_DONE));
    }

    @Override
    public void initialize(SparqlSelect definition, String componentUri, String graph) throws
            Component.InitializationFailed {
        // Store the resource URI, so we can reference it in messages.
        this.componentUri = componentUri;
    }

    @Override
    public void preExecution() {
        // No operation here.
    }

    @Override
    public void postExecution() {
        // No operation here.
    }

}
