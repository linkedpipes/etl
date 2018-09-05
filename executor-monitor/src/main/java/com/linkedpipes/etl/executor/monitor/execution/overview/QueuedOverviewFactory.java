package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.Execution;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QueuedOverviewFactory {

    public static final String DATETIME_TYPE =
            "http://www.w3.org/2001/XMLSchema#dateTime";

    private final DateFormat dateFormat = new
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode create(Execution execution) {
        ObjectNode contextNode = this.mapper.createObjectNode();
        contextNode.put("execution", LP_OVERVIEW.HAS_EXECUTION);
        contextNode.put("status", LP_OVERVIEW.HAS_STATUS);
        contextNode.put("pipeline", LP_OVERVIEW.HAS_PIPELINE);

        ObjectNode lastChangNode = mapper.createObjectNode();
        lastChangNode.put("@id", LP_OVERVIEW.HAS_LAST_CHANGE);
        lastChangNode.put("@type", DATETIME_TYPE);
        contextNode.set("lastChange", lastChangNode);

        ObjectNode rootNode = this.mapper.createObjectNode();
        rootNode.set("@context", contextNode);
        rootNode.put("@id", execution.getIri() + "/overview");

        ObjectNode executionNode = this.mapper.createObjectNode();
        executionNode.put("@id", execution.getIri());
        rootNode.set("execution", executionNode);

        ObjectNode statusNode = this.mapper.createObjectNode();
        statusNode.put("@id", LP_EXEC.STATUS_QUEUED);
        rootNode.set("status", statusNode);

        ObjectNode pipelineNode = this.mapper.createObjectNode();
        if (execution.getPipeline() != null) {
            pipelineNode.put("@id", execution.getPipeline().stringValue());
            rootNode.set("pipeline", pipelineNode);
        }

        // Use time of last change.
        Date lastChange;
        if (execution.getOverviewLastChange() == null) {
            lastChange = new Date();
        } else {
            lastChange = execution.getOverviewLastChange();
        }
        rootNode.put("lastChange", this.dateFormat.format(lastChange));

        return rootNode;
    }

}
