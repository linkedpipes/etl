package com.linkedpipes.etl.executor.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;
import com.linkedpipes.etl.rdf.utils.RdfFormatter;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Date;

public class ExecutionOverview {

    private final String executionIri;

    private String pipelineIri;

    private final File directory;

    private int componentsToExecute;

    private int componentsToMap;

    private int executedComponents;

    private int mappedComponents;

    private String pipelineStarted;

    private String pipelineFinished;

    private Long directorySize = null;

    private final RdfFormatter rdfFormat = new RdfFormatter();

    /**
     * Used only to read the status.
     */
    private final ExecutionStatusMonitor statusMonitor;

    private String lastChange;

    public ExecutionOverview(
            File directory,
            String executionIri,
            ExecutionStatusMonitor statusMonitor) {
        onAfterUpdate();
        this.directory = directory;
        this.executionIri = executionIri;
        this.statusMonitor = statusMonitor;
    }

    /**
     * Upon component begin the information about data units is written into
     * execution, so we need to change tha last change status.
     */
    public void onExecutionBegin(Date date) {
        pipelineStarted = rdfFormat.toXsdDate(date);
        onAfterUpdate();
    }

    public void onPipelineLoaded(PipelineModel pipeline) {
        pipelineIri = pipeline.getIri();
        componentsToExecute = 0;
        componentsToMap = 0;
        for (PipelineComponent component : pipeline.getComponents()) {
            if (component.isPlannedForExecution()) {
                ++componentsToExecute;
            } else if (component.isPlannedForMapping()) {
                ++componentsToMap;
                ++componentsToExecute;
            }
        }
        onAfterUpdate();
    }

    public void onComponentBegin() {
        onAfterUpdate();
    }

    private void onAfterUpdate() {
        this.lastChange = rdfFormat.toXsdDate(new Date());
    }

    public void onComponentMapped() {
        ++mappedComponents;
        onAfterUpdate();
    }

    public void onComponentExecuted() {
        ++executedComponents;
        onAfterUpdate();
    }

    public void onExecutionCancelling() {
        onAfterUpdate();
    }

    public void onExecutionEnd(Date date) {
        pipelineFinished = rdfFormat.toXsdDate(date);
        computeDirectorySize();
        onAfterUpdate();
    }

    private void computeDirectorySize() {
        this.directorySize = FileUtils.sizeOfDirectory(directory);
    }

    public ObjectNode toJsonLd(ObjectMapper mapper) {
        ObjectNode responseNode = mapper.createObjectNode();
        responseNode.set("@context", createContext(mapper));
        responseNode.put("@id", executionIri + "/overview");
        responseNode.put("@type", LP_OVERVIEW.OVERVIEW);

        ObjectNode pipelineNode = mapper.createObjectNode();
        if (pipelineIri != null) {
            pipelineNode.put("@id", pipelineIri);
        }
        responseNode.set("pipeline", pipelineNode);

        ObjectNode executionNode = mapper.createObjectNode();
        executionNode.put("@id", executionIri);
        responseNode.set("execution", executionNode);

        if (pipelineStarted != null) {
            responseNode.put("executionStarted", pipelineStarted);
        }
        if (pipelineFinished != null) {
            responseNode.put("executionFinished", pipelineFinished);
        }

        ObjectNode statusNode = mapper.createObjectNode();
        statusNode.put("@id", statusMonitor.getStatus().getIri());
        responseNode.set("status", statusNode);

        responseNode.put("lastChange", lastChange);

        ObjectNode executionProgressNode = mapper.createObjectNode();
        executionProgressNode.put("@id",
                executionIri + "/overview/executionProgress");
        executionProgressNode.put("total", componentsToExecute);
        executionProgressNode.put("current",
                executedComponents + mappedComponents);

        executionProgressNode.put("total_map", componentsToMap);
        executionProgressNode.put("current_mapped", mappedComponents);
        executionProgressNode.put("current_executed", executedComponents);

        responseNode.set("pipelineProgress", executionProgressNode);

        if (this.directorySize != null) {
            responseNode.put("directorySize", directorySize);
        }

        return responseNode;
    }

    private ObjectNode createContext(ObjectMapper mapper) {
        ObjectNode contextNode = mapper.createObjectNode();
        contextNode.put("pipeline", LP_OVERVIEW.HAS_PIPELINE);
        contextNode.put("execution", LP_OVERVIEW.HAS_EXECUTION);

        ObjectNode startNode = mapper.createObjectNode();
        startNode.put("@id", LP_OVERVIEW.HAS_START);
        startNode.put("@type", XSD.DATETIME);
        contextNode.set("executionStarted", startNode);

        ObjectNode finishedNode = mapper.createObjectNode();
        finishedNode.put("@id", LP_OVERVIEW.HAS_END);
        finishedNode.put("@type", XSD.DATETIME);
        contextNode.set("executionFinished", finishedNode);

        ObjectNode lastChangNode = mapper.createObjectNode();
        lastChangNode.put("@id", LP_OVERVIEW.HAS_LAST_CHANGE);
        lastChangNode.put("@type", XSD.DATETIME);
        contextNode.set("lastChange", lastChangNode);

        contextNode.put("status", LP_OVERVIEW.HAS_STATUS);
        contextNode.put("pipelineProgress", LP_OVERVIEW.HAS_PIPELINE_PROGRESS);
        contextNode.put("total", LP_OVERVIEW.HAS_PROGRESS_TOTAL);
        contextNode.put("current", LP_OVERVIEW.HAS_PROGRESS_CURRENT);
        contextNode.put("total_map", LP_OVERVIEW.HAS_PROGRESS_TOTAL_MAP);
        contextNode.put("current_mapped", LP_OVERVIEW.HAS_PROGRESS_MAPPED);
        contextNode.put("current_executed", LP_OVERVIEW.HAS_PROGRESS_EXECUTED);

        contextNode.put("directorySize", LP_OVERVIEW.HAS_DIRECTORY_SIZE);

        return contextNode;
    }

}
