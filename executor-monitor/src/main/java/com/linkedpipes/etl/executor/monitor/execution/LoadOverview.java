package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewEnricher;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewObject;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewToStatements;
import com.linkedpipes.etl.executor.monitor.execution.overview.QueuedOverviewFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * This class can be used only from a single thread.
 */
public class LoadOverview {

    private final ObjectMapper mapper = new ObjectMapper();

    private final QueuedOverviewFactory queuedFactory =
            new QueuedOverviewFactory();

    private final OverviewEnricher enricher = new OverviewEnricher();

    public void load(Execution execution) throws MonitorException {
        File file = this.getOverviewFile(execution);
        JsonNode overview;
        if (file.exists()) {
            overview = loadJson(file);
        } else {
            overview = this.queuedFactory.create(execution);
        }
        load(execution, overview);
    }

    private File getOverviewFile(Execution execution) {
        return new File(execution.getDirectory(), "execution-overview.jsonld");
    }

    private JsonNode loadJson(File file) throws MonitorException {
        try (InputStream stream = new FileInputStream(file)) {
            return mapper.readTree(stream);
        } catch (IOException ex) {
            throw new MonitorException("Can't load JSON from: {}", file, ex);
        }
    }

    public void load(Execution execution, JsonNode overview) {
        this.enricher.addMonitorInformation(execution, overview);
        execution.setOverviewJson(overview);
        this.updateExecutionFromOverview(execution);
    }

    private void updateExecutionFromOverview(Execution execution) {
        OverviewObject overview =
                OverviewObject.fromJson(execution.getOverviewJson());

        setLastChange(execution, overview);
        updateStatus(execution, overview);
        updateStatements(execution, overview);
    }

    private void setLastChange(Execution execution, OverviewObject overview) {
        Date lastOverviewChange = execution.getOverviewLastChange();
        if (lastOverviewChange != null &&
                lastOverviewChange.equals(overview.getLastChange())) {
            return;
        }
        // We know that there was a change, however it could have happen
        // deep in the past. Using that time client asking for changes since
        // given time would not get it.
        // To prevent this we use local time value instead.
        execution.setLastChange(new Date());

        // Set time from the overview.
        execution.setOverviewLastChange(overview.getLastChange());
    }

    private void updateStatus(Execution execution, OverviewObject overview) {
        ExecutionStatus oldStatus = execution.getStatus();
        ExecutionStatus newStatus =
                ExecutionStatus.fromIri(overview.getStatus());
        // Postpone failed and finished until the execution time end is set.
        switch (newStatus) {
            case FAILED:
                if (isFinished(overview)) {
                    execution.setStatus(ExecutionStatus.FAILED);
                } else {
                    execution.setStatus(ExecutionStatus.RUNNING);
                }
                break;
            case FINISHED:
                if (isFinished(overview)) {
                    execution.setStatus(ExecutionStatus.FINISHED);
                } else {
                    execution.setStatus(ExecutionStatus.RUNNING);
                }
                break;
            case RUNNING:
                if (execution.getStatus() == null) {
                    // Initial load.
                    execution.setStatus(ExecutionStatus.DANGLING);
                    break;
                }
                if (execution.hasExecutor()) {
                    execution.setStatus(ExecutionStatus.RUNNING);
                } else {
                    if (oldStatus == ExecutionStatus.RUNNING) {
                        execution.setStatus(ExecutionStatus.UNRESPONSIVE);
                    } else {
                        // Keep previous status.
                    }
                }
                break;
            default:
                // We quit the function here as we use the status from overview.
                execution.setStatus(newStatus);
                return;
        }
        // We need to update the status in the overview.
        StatusSetter.updateOverview(execution);
        overview.setStatus(execution.getStatus().asStr());
    }

    private boolean isFinished(OverviewObject overview) {
        return overview.getFinish() != null;
    }

    private void updateStatements(Execution execution, OverviewObject overview) {
        OverviewToStatements overviewToStatements = new OverviewToStatements();
        execution.setOverviewStatements(
                overviewToStatements.asStatements(execution, overview));
    }


}
