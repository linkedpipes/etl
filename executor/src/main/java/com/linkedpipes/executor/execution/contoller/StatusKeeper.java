package com.linkedpipes.executor.execution.contoller;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.event.ComponentBegin;
import com.linkedpipes.etl.executor.api.v1.event.ComponentEnd;
import com.linkedpipes.etl.executor.api.v1.event.ComponentProgress;
import com.linkedpipes.commons.entities.executor.ExecutionStatus;
import com.linkedpipes.commons.entities.rest.Progress;
import com.linkedpipes.commons.entities.executor.ExecutionStatusCode;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;
import com.linkedpipes.executor.execution.entity.event.ExecutionFailed;
import com.linkedpipes.executor.rdf.boundary.MessageStorage;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Škoda Petr
 */
public class StatusKeeper implements MessageStorage.MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(StatusKeeper.class);

    private final ExecutionStatus status = new ExecutionStatus();

    private final ObjectMapper json = new ObjectMapper();

    private ResourceManager resourceManager;

    public StatusKeeper(String executionId, String executionUri) {
        status.setId(executionId);
        status.setUri(executionUri);
        status.setRunning(true);
    }

    /**
     * Must be called before status can be saved to hard drive.
     *
     * @param resourceManager
     */
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void setPipeline(PipelineConfiguration pipeline) {
        status.setPipelineUri(pipeline.getUri());
        for (PipelineConfiguration.Component component : pipeline.getComponents()) {
            final String uri = component.getUri();
            status.getComponents().put(uri, new ExecutionStatus.Component(uri));
        }
        status.setProgress(new Progress(0, pipeline.getComponents().size(), new Date()));
        persist();
    }

    @Override
    public void onMesssage(Event message) {
        if (message instanceof ComponentBegin) {
            final String uri = ((ComponentBegin) message).getComponentUri();
            status.getComponents().get(uri).setStatus(ExecutionStatusCode.RUNNING);
            status.getComponents().get(uri).setStart(new Date());
        } else if (message instanceof ComponentProgress) {
            final ComponentProgress progress = (ComponentProgress) message;
            final String uri = progress.getComponentUri();
            status.getComponents().get(uri).setProgress(
                    new Progress(progress.getCurrent(), progress.getTotal(), new Date()));
        } else if (message instanceof ComponentEnd) {
            final String uri = ((ComponentEnd) message).getComponentUri();
            status.getComponents().get(uri).setStatus(ExecutionStatusCode.FINISHED);
            // Update pipeline progress.
            status.getProgress().setCurrent(status.getProgress().getCurrent() + 1);
        } else if (message instanceof ExecutionFailed) {
            status.setPipelineStatus(ExecutionStatusCode.FAILED);
        }
        persist();
    }

    public void initializationFailed(Throwable exception, String message, Object... args) {
        status.setTerminationException(exception.getMessage());
        status.setPipelineStatus(ExecutionStatusCode.INITIALIZATION_FAILED);
        persist();
    }

    public void initializationFailed(String message, Object... args) {
        status.setPipelineStatus(ExecutionStatusCode.INITIALIZATION_FAILED);
        persist();
    }

    public void pipelineStarts() {
        status.setExecutionStart(new Date());
        status.setPipelineStatus(ExecutionStatusCode.RUNNING);
        persist();
    }

    /**
     * Must be called at the very end of execution.
     */
    public void pipelineEnd() {
        status.setRunning(false);
        status.setExecutionEnd(new Date());
        if (status.getPipelineStatus() == ExecutionStatusCode.RUNNING) {
            status.setPipelineStatus(ExecutionStatusCode.FINISHED);
        }
        // Get resource directory size.
        status.setExecutionDirectorySize(FileUtils.sizeOfDirectory(resourceManager.getRoot()));
        // Save to disk.
        persist();
    }

    public void throwable(Throwable exception) {
        status.setTerminationException(exception.getMessage());
        status.setExecutionEnd(new Date());
        status.setPipelineStatus(ExecutionStatusCode.FAILED_ON_THROWABLE);
        persist();
    }

    /**
     * Write content on {@link executionStatus} to disk and also update lastModification. Should be called after any
     * change in the {@link #status}.
     */
    protected void persist() {
        status.setLastModification(new Date());
        // Save to hard drive.
        if (resourceManager == null) {
            return;
        }
        final File statusFile = resourceManager.getStatusFile();
        try {
            json.writerWithDefaultPrettyPrinter().writeValue(statusFile, status);
        } catch (IOException ex) {
            LOG.error("Can't write status file!", ex);
        }
    }

    public ExecutionStatus getStatus() {
        return status;
    }

}
