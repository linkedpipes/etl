package com.linkedpipes.executor.monitor.execution.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linkedpipes.commons.entities.executor.ExecutionStatus;
import com.linkedpipes.commons.entities.executor.Labels;
import com.linkedpipes.commons.entities.executor.monitor.ExecutionBasic;
import com.linkedpipes.commons.entities.executor.ExecutionStatusCode;
import com.linkedpipes.executor.monitor.execution.entity.ExecutionMetadata;
import com.linkedpipes.executor.monitor.execution.entity.ExecutionMetadata.Status;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 *
 * @author Škoda Petr
 */
public final class ExecutionHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionHelper.class);

    /**
     *
     * @param record
     * @param language
     * @param baseUri Execution URI prefix.
     * @return
     */
    public static ExecutionBasic createExecution(ExecutionMetadata record, String language, String baseUri) {
        final String executionUri = baseUri + record.getId();
        if (record.getStatus() == Status.QUEUED) {
            // Create artificial information as the pipeline is not yet running.
            final ExecutionBasic execution = new ExecutionBasic();

            execution.setId(record.getId());
            // Here we need to construct the URI.

            execution.setUri(executionUri);
            execution.setLabel(record.getLabel().getLabel(record.getPipelineUri(), language));
            execution.setStatusCode(ExecutionStatusCode.QUEUED.getCode());
            execution.setRunning(false);
            execution.setPipelineUri(record.getPipelineUri());

            return execution;
        }
        final ObjectMapper jsonMapper = new ObjectMapper();
        final ExecutionStatus status;
        try {
            status = jsonMapper.readValue(new File(record.getDirectory(), PathDefinitions.STATUS), ExecutionStatus.class);
        } catch (IOException ex) {
            // Execution might have been deleted.
            LOG.error("Can't load data for: {} directory: {}", PathDefinitions.STATUS, record.getDirectory(), ex);
            return null;
        }
        // Load and apply labels.
        Labels labels = null;
        try {
            labels = jsonMapper.readValue(new File(record.getDirectory(), PathDefinitions.LABELS), Labels.class);
        } catch (IOException ex) {
            LOG.error("Can't load data for: {} directory: {}", PathDefinitions.LABELS, record.getDirectory(), ex);
        }
        final ExecutionBasic execution = new ExecutionBasic();

        execution.setEnd(status.getExecutionEnd());
        execution.setId(status.getId());
        execution.setUri(status.getUri());
        execution.setLabel(record.getLabel().getLabel(record.getPipelineUri(), language));
        execution.setLastUpdate(status.getLastModification());
        execution.setPipelineProgress(status.getProgress());
        execution.setPipelineUri(status.getPipelineUri());
        execution.setStart(status.getExecutionStart());
        execution.setStatusCode(status.getPipelineStatus().getCode());
        execution.setRunning(status.isRunning());
        execution.setExecutionDirectorySize(status.getExecutionDirectorySize());

        if (execution.isRunning()) {
            // For running include information about current state, look for curent component.
            for (Map.Entry<String, ExecutionStatus.Component> component : status.getComponents().entrySet()) {
                if (component.getValue().getStatus() == ExecutionStatusCode.RUNNING) {
                    execution.setCurrentComponentLabel(getLabel(labels, component.getKey(), language));
                    execution.setCurrentComponentProgress(component.getValue().getProgress());
                    break;
                }
            }
        }
        return execution;
    }

    /**
     * Update given execution record from given directory.
     *
     * @param execution
     */
    public static void updateExecution(ExecutionMetadata execution) {
        final File directory = execution.getDirectory();
        // Load current execution file.
        final ObjectMapper jsonMapper = new ObjectMapper();
        final ExecutionStatus status;
        try {
            status = jsonMapper.readValue(new File(directory, PathDefinitions.STATUS), ExecutionStatus.class);
        } catch (IOException ex) {
            LOG.error("Can't load data from file.", ex);
            // TODO We can still show the information about execution, or should we straight delete it?
            return;
        }
        // Check for change.
        if (execution.getLastChange() == null || status.getLastModification().after(execution.getLastChange())) {
            // There was an update.
            execution.setLastChange(status.getLastModification());
            execution.setUpdateTime(new Date());
            // Update metadata.
            if (!status.isRunning() && execution.getStatus() == Status.RUNNING) {
                execution.setStatus(Status.DONE);
                // Save the record.
                final File taskFile = new File(execution.getDirectory(), "task.json");
                try (OutputStream outputStream = Files.newOutputStream(taskFile.toPath())) {
                    jsonMapper.writeValue(outputStream, execution);
                } catch (IOException ex) {
                    // TODO We should try later, as otherwise
                    LOG.error("Can't write task file!", ex);
                }
            }
        }
    }

    protected static String getLabel(Labels labelsMap, String uri, String language) {
        if (labelsMap == null) {
            return uri;
        }
        return labelsMap.getLabel(uri, language);
    }

}
