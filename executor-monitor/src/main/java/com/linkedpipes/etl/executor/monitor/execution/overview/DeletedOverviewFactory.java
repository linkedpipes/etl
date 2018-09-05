package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeletedOverviewFactory {

    private static final String DATETIME_TYPE =
            "http://www.w3.org/2001/XMLSchema#dateTime";

    private static final DateFormat DATE_FORMAT = new
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static JsonNode create(Execution execution, Date date) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode contextNode = mapper.createObjectNode();
        contextNode.put("execution", LP_OVERVIEW.HAS_EXECUTION);
        contextNode.put("status", LP_OVERVIEW.HAS_STATUS);

        ObjectNode lastChangNode = mapper.createObjectNode();
        lastChangNode.put("@id", LP_OVERVIEW.HAS_LAST_CHANGE);
        lastChangNode.put("@type", DATETIME_TYPE);
        contextNode.set("lastChange", lastChangNode);

        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("@context", contextNode);
        rootNode.put("@id", execution.getIri() + "/overview");

        ObjectNode executionNode = mapper.createObjectNode();
        executionNode.put("@id", execution.getIri());
        rootNode.set("execution", executionNode);

        ObjectNode statusNode = mapper.createObjectNode();
        statusNode.put("@id", ExecutionStatus.DELETED.asStr());
        rootNode.set("status", statusNode);

        String lastChange = DATE_FORMAT.format(date);
        rootNode.put("lastChange", lastChange);

        return rootNode;
    }

}
