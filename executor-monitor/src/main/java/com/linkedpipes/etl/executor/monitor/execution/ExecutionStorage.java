package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade.ExecutionMismatch;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade.OperationFailed;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade.UnknownExecution;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Responsible for storing information about existing executions.
 *
 * @author Petr Škoda
 */
@Service
class ExecutionStorage {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutionStorage.class);

    @Autowired
    private Configuration configuration;

    /**
     * List of executions.
     */
    private final List<Execution> executions = new ArrayList<>(64);

    /**
     * Directories of this executions should be deleted.
     */
    private final List<Execution> executionToDelete = new ArrayList<>(16);

    @PostConstruct
    protected void onInit() {
        final Date updateTime = new Date();
        for (File directory : configuration.getWorkingDirectory().listFiles()) {
            if (!directory.isDirectory()) {
                continue;
            }
            try {
                final Execution execution = create(directory);
                if (execution == null) {
                    LOG.warn("Invalid directory: {}", directory);
                    continue;
                }
                // All running pipelines are considered to be DANGLING as
                // we do not know if their executors are running or not.
                if (execution.getStatus() == Execution.StatusType.RUNNING) {
                    execution.setStatus(Execution.StatusType.DANGLING);
                    ExecutionChecker.updateGenerated(execution);
                }
                execution.setLastCheck(updateTime);
                executions.add(execution);
            } catch (Exception ex) {
                LOG.error("Can't load execution from: {}", directory, ex);
            }
        }
    }

    /**
     *
     * @return Unmodifiable list.
     */
    public List<Execution> getExecutions() {
        return Collections.unmodifiableList(executions);
    }

    /**
     * Return execution with given ID. ID is the last part of
     * the execution IRI after the last '/'.
     *
     * @param id
     * @return Null if no such execution exists.
     */
    public Execution getExecution(String id) {
        for (Execution execution : executions) {
            if (execution.getId().equals(id)) {
                return execution;
            }
        }
        return null;
    }

    /**
     * Create new execution from given stream. The execution is added
     * with the QUEUED status.
     *
     * @param stream Stream with pipeline definition.
     * @param format File extension - format.
     * @return Newly created execution.
     */
    public Execution createExecution(InputStream stream, RDFFormat format)
            throws OperationFailed {
        final String uuid = UUID.randomUUID().toString();
        final File directory = new File(
                configuration.getWorkingDirectory(), uuid);
        final File definitionFile = new File(directory,
                "definition" + File.separator + "definition."
                + format.getDefaultFileExtension());
        definitionFile.getParentFile().mkdirs();
        // Save stream content into the definition file.
        try {
            Files.copy(stream, definitionFile.toPath());
        } catch (IOException ex) {
            throw new OperationFailed("Can't copy definition file.", ex);
        }
        //
        final Execution newExecution = new Execution();
        newExecution.setIri(configuration.getExecutionPrefix() + uuid);
        newExecution.setDirectory(directory);
        // Load data.
        try {
            ExecutionChecker.updateFromDirectory(newExecution);
        } catch (ExecutionMismatch ex) {
            throw new OperationFailed("", ex);
        }
        try {
            PipelineLoader.loadPipeline(newExecution);
        } catch (OperationFailed | IOException ex) {
            throw new OperationFailed("Can't load pipeline.", ex);
        }
        executions.add(newExecution);
        return newExecution;
    }

    /**
     * Delete given execution.
     *
     * @param execution
     */
    public void delete(Execution execution) {
        if (execution == null) {
            return;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 10);
        execution.setTimeToLive(calendar.getTime());
        // Update content.
        ExecutionChecker.setToDeleted(execution);
        // Clear statements about the pipeline as a tombstone
        // does not need them.
        execution.setPipelineStatements(Collections.EMPTY_LIST);
        // Queued for delete.
        executionToDelete.add(execution);
    }

    /**
     * Check of the execution status from a directory.
     *
     * @param execution
     */
    public void checkExecution(Execution execution) {
        switch (execution.getStatus()) {
            case FINISHED:
            case DELETED:
                // Do nothing here.
                break;
            default:
                try {
                    ExecutionChecker.updateFromDirectory(execution);
                } catch (OperationFailed | ExecutionMismatch ex) {
                    LOG.warn("Can't update execution.", ex);
                }
        }
    }

    /**
     * Perform updateFromDirectory of given execution from the stream.
     *
     * @param execution Execution data in JSONLD.
     * @param stream
     */
    public void checkExecution(Execution execution, InputStream stream)
            throws ExecutionMismatch, OperationFailed {
        ExecutionChecker.checkExecution(execution, stream);
    }

    /**
     * Discover and return execution instance for execution content in the
     * given stream.
     *
     * Use to determine the execution based only on the RDF content.
     *
     * @param stream Execution data in JSONLD.
     * @return
     * @throws UnknownExecution
     * @throws OperationFailed
     */
    public Execution discover(InputStream stream)
            throws UnknownExecution, OperationFailed {
        final Execution newExecution = new Execution();
        try {
            ExecutionChecker.checkExecution(newExecution, stream);
        } catch (ExecutionMismatch ex) {
            throw new OperationFailed("Can't load execution.", ex);
        }
        // Search for the execution in our list.
        for (Execution execution : executions) {
            if (execution.getIri().equals(newExecution.getIri())) {
                // Update data.
                execution.setExecutionStatements(
                        execution.getExecutionStatements());
                execution.setLastChange(newExecution.getLastChange());
                execution.setLastCheck(newExecution.getLastCheck());
                // We can change status from queud to running only.
                if (newExecution.getStatus() == Execution.StatusType.RUNNING
                        && execution.getStatus() == Execution.StatusType.QUEUED) {
                    execution.setStatus(Execution.StatusType.RUNNING);
                }
                //
                return execution;
            }
        }
        throw new UnknownExecution();
    }

    /**
     * Load execution from given directory and return it.
     *
     * @param directory
     * @return Null if the directory does not represent a valid execution.
     */
    private Execution create(File directory)
            throws OperationFailed, ExecutionMismatch {
        final Execution execution = new Execution();
        // Determine IR from the directory name.
        execution.setIri(configuration.getExecutionPrefix()
                + directory.getName());
        //
        execution.setDirectory(directory);
        ExecutionChecker.updateFromDirectory(execution);
        try {
            PipelineLoader.loadPipeline(execution);
        } catch (OperationFailed | IOException ex) {
            throw new OperationFailed("Can't load pipeline.", ex);
        }
        return execution;
    }

    /**
     * Call to updateFromDirectory non-finished execution from a directory.
     */
    @Scheduled(fixedDelay = 15000, initialDelay = 200)
    protected void checkDirectory() {
        final Date now = new Date();
        // Update only such execution that were not updated in last
        // 10 seconds. As the execution could have been updated
        // from a REST service and we do not want to update it twice.
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, -10);
        final Date requiredLastUpdate = calendar.getTime();
        for (Execution execution : executions) {
            if (requiredLastUpdate.after(execution.getLastCheck())) {
                checkExecution(execution);
            }
        }
        // Delete tombstones.
        final Collection<Execution> toDelete = new ArrayList<>(2);
        for (Execution execution : executions) {
            if (execution.getStatus() == Execution.StatusType.DELETED) {
                // Check if delete pipeline or not.
                if (execution.getTimeToLive().before(now)) {
                    // Drop execution.
                    toDelete.add(execution);
                }
            }
        }
        executions.removeAll(toDelete);
        // Try to delete directories.
        final Collection<Execution> toRemove = new ArrayList<>(2);
        for (Execution execution : executionToDelete) {
            try {
                FileUtils.deleteDirectory(execution.getDirectory());
                toRemove.add(execution);
            } catch (IOException ex) {
                // The directory can be used by other process or user,
                // so it may take more atteps to delete the directory.
            }
        }
        executionToDelete.removeAll(toRemove);

    }

}
