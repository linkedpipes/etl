package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewFactory;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewObject;
import com.linkedpipes.etl.executor.monitor.executor.ExecutionSource;
import com.linkedpipes.etl.executor.monitor.executor.Executor;
import com.linkedpipes.etl.executor.monitor.executor.ExecutorEventListener;
import com.linkedpipes.etl.rdf4j.Statements;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Responsible for storing information about existing executions.
 */
@Service
class ExecutionStorage
        implements ExecutionSource, ExecutorEventListener {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutionStorage.class);

    private static final int TOMBSTONE_TTL = 5 * 60;

    private final Configuration configuration;

    private final List<Execution> executions = new ArrayList<>(64);

    private final List<File> directoriesToDelete = new ArrayList<>(16);

    private final Map<Executor, Execution> executors = new HashMap<>();

    @Autowired
    public ExecutionStorage(Configuration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void onInit() throws MonitorException {
        File executionsDirectory = getExecutionsDirectory();
        Arrays.stream(executionsDirectory.listFiles())
                .filter(file -> file.isDirectory())
                .forEach(directory -> loadExecutionForFirstTime(directory));
    }

    private File getExecutionsDirectory() throws MonitorException {
        File directory = configuration.getWorkingDirectory();
        if (!directory.isDirectory()) {
            throw new MonitorException(
                    "Execution working directory does not exists");
        }
        return directory;
    }

    private Execution loadExecutionForFirstTime(File directory) {
        final Date updateTime = new Date();

        Execution execution = new Execution();
        execution.setIri(getExecutionIri(directory));
        execution.setDirectory(directory);

        PipelineLoader pipelineLoader = new PipelineLoader(execution);
        try {
            pipelineLoader.loadPipelineIntoExecution();
        } catch (Throwable ex) {
            LOG.error("Can't load pipeline for: {}", directory, ex);
            return null;
        }

        LoadOverview overviewLoader = new LoadOverview();
        try {
            overviewLoader.load(execution);
        } catch (Throwable ex) {
            LOG.error("Can't load overview for: {}", directory, ex);
            return null;
        }

        if (!ExecutionStatus.QUEUED.equals(execution.getStatus())) {
            try {
                updateDebugData(execution);
            } catch (Throwable ex) {
                LOG.error("Can't load debug data for: {}", directory, ex);
                return null;
            }
        }

        if (ExecutionStatus.isFinished(execution.getStatus())) {
            execution.setHasFinalData(true);
        }

        ExecutionMigration migration = new ExecutionMigration();
        if (migration.shouldMigrate(execution)) {
            migration.migrate(execution);
        }

        execution.setLastChange(updateTime);
        this.executions.add(execution);
        return execution;
    }

    private String getExecutionIri(File directory) {
        return this.configuration.getExecutionPrefix() + directory.getName();
    }

    private void updateDebugData(Execution execution) throws MonitorException {
        ExecutionLoader executionLoader = new ExecutionLoader();
        updateFromExecution(
                execution, executionLoader.loadStatements(execution));
    }

    public List<Execution> getExecutions() {
        return Collections.unmodifiableList(executions);
    }

    /**
     * Return last part of the execution IRI after the last '/'.
     */
    public Execution getExecution(String id) {
        for (Execution execution : executions) {
            if (execution.getId().equals(id)) {
                return execution;
            }
        }
        return null;
    }

    @Override
    public Execution getExecution(JsonNode overview) {
        String iri;
        try {
            iri = OverviewObject.getIri(overview);
        } catch (Exception ex) {
            LOG.error("Invalid overview object.", ex);
            return null;
        }
        for (Execution execution : executions) {
            if (execution.getIri().equals(iri)) {
                return execution;
            }
        }
        return null;
    }

    @Override
    public Execution getExecution(Executor executor) {
        return executors.get(executor);
    }

    /**
     * Perform full execution update from directory.
     */
    public void update(Execution execution) {
        if (!(shouldUpdate(execution))) {
            // We do not reload dangling or invalid executions.
            return;
        }
        LoadOverview overviewLoader = new LoadOverview();
        try {
            overviewLoader.load(execution);
        } catch (MonitorException ex) {
            LOG.error("Can't update execution overview for: {}",
                    execution.getId(), ex);
        }
        if (ExecutionStatus.QUEUED == execution.getStatus()) {
            // There is nothing more then the overview.
            return;
        }
        try {
            updateDebugData(execution);
        } catch (MonitorException ex) {
            LOG.error("Can't update debug data for: {}",
                    execution.getId(), ex);
            return;
        }
        if (ExecutionStatus.isFinished(execution.getStatus())) {
            execution.setHasFinalData(true);
        }
    }

    private void updateFromOverview(Execution execution, JsonNode overview) {
        LoadOverview overviewLoader = new LoadOverview();
        overviewLoader.load(execution, overview);
    }

    /**
     * Updates only from execution data (debug data).
     */
    public void updateFromExecution(
            Execution execution, Statements statements) {
        execution.setDebugData(new DebugData(statements, execution));
    }

    public Execution createExecution(
            Collection<Statement> pipeline, List<MultipartFile> inputs)
            throws MonitorException {
        String uuid = createExecutionGuid();
        File directory = new File(configuration.getWorkingDirectory(), uuid);
        try {
            ExecutionFactory.prepareExecutionInDirectory(
                    directory, pipeline, inputs);
        } catch (MonitorException ex) {
            deleteDirectory(directory);
            throw ex;
        }
        Execution execution = loadExecutionForFirstTime(directory);
        if (execution == null) {
            throw new MonitorException("Can't load new execution");
        }
        return execution;
    }

    private String createExecutionGuid() {
        return (new Date()).getTime() + "-"
                + Integer.toString(executions.size()) + "-"
                + UUID.randomUUID().toString();
    }

    private void deleteDirectory(File directory) {
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException ex) {
            LOG.error("Can't delete directory.", ex);
        }
    }

    public void delete(Execution execution) {
        if (execution == null) {
            return;
        }
        execution.setTimeToLive(getNowShiftedBySeconds(TOMBSTONE_TTL));
        // Clear statements that we do not need any more.
        execution.setPipelineStatements(Collections.emptyList());
        OverviewFactory overviewFactory = new OverviewFactory();
        updateFromOverview(
                execution,
                overviewFactory.createDeleted(execution, new Date()));
        this.directoriesToDelete.add(execution.getDirectory());
    }

    private Date getNowShiftedBySeconds(int secondsChange) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, secondsChange);
        return calendar.getTime();
    }

    @Scheduled(fixedDelay = 15000, initialDelay = 200)
    public void updateExecutions() {
        for (Execution execution : executions) {
            if (shouldUpdate(execution)) {
                update(execution);
            }
        }
        this.deleteTombstones(new Date());
        this.deleteDirectories();
    }

    private boolean shouldUpdate(Execution execution) {
        if (execution.getStatus() == ExecutionStatus.DELETED
                || execution.getStatus() == ExecutionStatus.INVALID) {
            return false;
        }
        return !execution.isHasFinalData();
    }

    private void deleteTombstones(Date time) {
        Collection<Execution> toDelete = new ArrayList<>(2);
        for (Execution execution : executions) {
            if (execution.getStatus() == ExecutionStatus.DELETED) {
                if (execution.getTimeToLive().before(time)) {
                    toDelete.add(execution);
                }
            }
        }
        this.executions.removeAll(toDelete);
    }

    private void deleteDirectories() {
        List<File> toRemove = new ArrayList<>(2);
        for (File directory : directoriesToDelete) {
            try {
                FileUtils.deleteDirectory(directory);
                toRemove.add(directory);
            } catch (IOException ex) {
                // The directory can be used by other process or user,
                // so it may take more attempts to delete the directory.
            }
        }
        directoriesToDelete.removeAll(toRemove);
    }

    @Override
    public void onExecutorHasExecution(Execution execution, Executor executor) {
        LOG.info("onExecutorHasExecution: {}",
                execution == null ? "null" : execution.getId());
        Execution oldExecution = executors.get(executor);
        if (oldExecution == null) {
            executors.put(executor, execution);
            execution.setExecutor(true);
            execution.setExecutorResponsive(true);
            return;
        }
        if (execution == oldExecution) {
            // There is no change in the execution.
            return;
        }
        // Executor stop execution of one execution and start with another.
        oldExecution.setExecutor(false);
        oldExecution.setExecutorResponsive(false);
        if (execution == null) {
            executors.remove(executor);
        } else {
            executors.put(executor, execution);
            execution.setExecutor(true);
            execution.setExecutorResponsive(true);
        }
    }

    @Override
    public void onExecutorWithoutExecution(Executor executor) {
        // We have execution assigned to this executor, but now it is not
        // executing anything. We need to update from disk as the
        // execution might have been finished in a meantime.
        Execution execution = executors.get(executor);
        if (execution == null) {
            return;
        }
        execution.setExecutor(false);
        execution.setExecutorResponsive(false);
        executors.remove(executor);
        update(execution);
    }

    @Override
    public void onExecutorUnavailable(Executor executor) {
        Execution execution = executors.get(executor);
        if (execution == null) {
            return;
        }
        execution.setExecutorResponsive(false);
    }

    @Override
    public void onOverview(Execution execution, JsonNode overview) {
        updateFromOverview(execution, overview);
    }

}
