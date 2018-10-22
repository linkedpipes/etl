package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewToListStatements;

import java.util.Date;

/**
 * Update execution status (outside overview load) and reflect changes
 * where needed.
 *
 * Can be used for example to reflect status change by losing executor.
 */
class StatusSetter {

    public static void setStatus(Execution execution, ExecutionStatus status) {
        Date now = new Date();
        execution.setStatus(status);
        execution.setLastChange(now);
        updateOverview(execution);
        updateOverviewStatements(execution);
    }

    public static void updateOverview(Execution execution) {
        JsonNode root = execution.getOverviewJson();
        ObjectNode status = (ObjectNode)root.get("status");
        status.remove("@id");
        status.put("@id", execution.getStatus().asStr());
    }

    private static void updateOverviewStatements(Execution execution) {
        OverviewToListStatements overviewToStatements = new OverviewToListStatements();
        execution.setOverviewStatements(overviewToStatements.asStatements(
                execution, execution.getOverviewJson()));
    }

}
