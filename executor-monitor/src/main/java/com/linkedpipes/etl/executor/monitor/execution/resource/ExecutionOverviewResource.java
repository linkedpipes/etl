package com.linkedpipes.etl.executor.monitor.execution.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.Execution;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExecutionOverviewResource implements LoadableResource {

    private JsonNode overviewRoot = null;

    private boolean hasExecutor = false;

    @Override
    public void missing(Execution execution) {
        final ObjectMapper mapper = new ObjectMapper();

        final ObjectNode contextNode = mapper.createObjectNode();
        contextNode.put("execution", LP_OVERVIEW.HAS_EXECUTION);
        contextNode.put("status", LP_OVERVIEW.HAS_STATUS);

        final ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("@context", contextNode);
        rootNode.put("@id", execution.getIri() + "/overview");

        final ObjectNode executionNode = mapper.createObjectNode();
        executionNode.put("@id", execution.getIri());
        rootNode.set("execution", executionNode);

        final ObjectNode statusNode = mapper.createObjectNode();
        statusNode.put("@id",
                "http://etl.linkedpipes.com/resources/status/queued");
        rootNode.set("status", statusNode);
        
        overviewRoot = rootNode;
    }

    @Override
    public void load(InputStream stream) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        overviewRoot = mapper.readTree(stream);
        update();
    }

    public void setHasExecutor(boolean hasExecutor) {
        this.hasExecutor = hasExecutor;
    }

    @Override
    public void writeToStream(OutputStream stream) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        if (overviewRoot != null) {
            mapper.writeValue(stream, overviewRoot);
        }
    }

    @Override
    public String getRelativeUrlPath() {
        return "/api/v1/executions/overview";
    }

    @Override
    public String getRelativeFilePath() {
        return "/execution/overview.jsonld";
    }

    private void update() {
        if (hasExecutor) {
            return;
        }
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = (ObjectNode) overviewRoot;
        switch (root.get("status").asText()) {
            case "http://etl.linkedpipes.com/resources/status/running":
            case "http://etl.linkedpipes.com/resources/status/cancelling":
                final ObjectNode statusNode = mapper.createObjectNode();
                root.put("@id",
                        "http://etl.linkedpipes.com/resources/status/unknown");
                root.set("status", statusNode);
                break;
            default:
                break;
        }

    }

}
