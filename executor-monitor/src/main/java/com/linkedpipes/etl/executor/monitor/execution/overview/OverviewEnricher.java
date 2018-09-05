package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_MONITOR;
import com.linkedpipes.etl.executor.monitor.execution.Execution;

/**
 * Add information from executor monitor.
 */
public class OverviewEnricher {

    public void addMonitorInformation(Execution execution, JsonNode node) {
        ObjectNode root = (ObjectNode)node;
        ObjectNode context = (ObjectNode)root.get("@context");

        context.put("finalData", LP_MONITOR.HAS_FINAL_DATA);

        root.put("finalData", execution.isHasFinalData());
    }

}
