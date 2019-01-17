package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_MONITOR;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewFactory;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewObject;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewToListStatements;

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

    private final OverviewFactory overviewFactory = new OverviewFactory();

    public void load(Execution execution) throws MonitorException {
        File file = getOverviewFile(execution);
        JsonNode overview;
        if (file.exists()) {
            overview = loadJson(file);
        } else {
            overview = overviewFactory.createQueued(execution);
        }
        load(execution, overview);
    }

    public void load(Execution execution, JsonNode overview) {
        Date oldLastUpdate = execution.getLastOverviewChange();
        if (oldLastUpdate != null) {
            Date newLastUpdate = OverviewObject.getLastChange(overview);
            if (oldLastUpdate.after(newLastUpdate)) {
                // We have never version, do not update.
                return;
            }
        }
        addMonitorInformation(execution, overview);
        execution.setOverviewJson(overview);
        updateExecutionFromOverview(execution);
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

    public void addMonitorInformation(Execution execution, JsonNode node) {
        ObjectNode root = (ObjectNode) node;
        ObjectNode context = (ObjectNode) root.get("@context");
        context.put("finalData", LP_MONITOR.HAS_FINAL_DATA);
        root.put("finalData", execution.isHasFinalData());
    }

    private void updateExecutionFromOverview(Execution execution) {
        OverviewObject overview =
                OverviewObject.fromJson(execution.getOverviewJson());
        UpdateExecutionStatus updater = new UpdateExecutionStatus();
        boolean statusChanged = updater.update(execution, overview);
        updateStatements(execution, overview);
        setLastChange(execution, overview, statusChanged);
    }

    private void updateStatements(
            Execution execution, OverviewObject overview) {
        OverviewToListStatements overviewToStatements =
                new OverviewToListStatements();
        execution.setOverviewStatements(
                overviewToStatements.asStatements(execution, overview));
    }

    private void setLastChange(
            Execution execution, OverviewObject overview,
            boolean statusChanged) {
        if (!statusChanged && !hasOverviewChanged(execution, overview)) {
            return;
        }
        // We know that there was a change, however it could have happen
        // deep in the past. Using that time client asking for changes since
        // given time would not get it.
        // To prevent this we use local time value instead.
        execution.setLastChange(new Date());

        // Update overview change time.
        execution.setLastOverviewChange(overview.getLastChange());
    }

    private boolean hasOverviewChanged(
            Execution execution, OverviewObject overview) {
        Date change = execution.getLastOverviewChange();
        return change == null || !change.equals(overview.getLastChange());
    }

}
