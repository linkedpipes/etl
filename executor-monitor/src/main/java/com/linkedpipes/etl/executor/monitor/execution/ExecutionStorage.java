package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.executor.monitor.execution.overview.DeletedOverviewFactory;
import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewObject;
import com.linkedpipes.etl.executor.monitor.executor.ExecutionSource;
import com.linkedpipes.etl.rdf4j.Statements;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * Responsible for storing information about existing executions.
 */
@Service
class ExecutionStorage implements ExecutionSource {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutionStorage.class);

    private final Configuration configuration;

    private final List<Execution> executions = new ArrayList<>(64);

    private final List<File> directoriesToDelete = new ArrayList<>(16);

    @Autowired
    public ExecutionStorage(Configuration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void onInit() throws MonitorException {
        File rootDirectory = this.configuration.getWorkingDirectory();
        if (!rootDirectory.isDirectory()) {
            throw new MonitorException(
                    "Execution working directory does not exists");
        }
        Arrays.stream(rootDirectory.listFiles())
                .filter(file -> file.isDirectory())
                .forEach(directory -> loadExecutionForFirstTime(directory));
    }

    private Execution loadExecutionForFirstTime(File directory) {
        Date updateTime = new Date();

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
        updateExecution(execution, executionLoader.loadStatements(execution));
    }

    public List<Execution> getExecutions() {
        return Collections.unmodifiableList(executions);
    }

    /**
     * @param id Last part of the execution IRI after the last '/'.
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

    /**
     * Perform full execution update from directory.
     */
    public void update(Execution execution) {
        if (!(shouldUpdate(execution))){
            // We do not reload dangling or invalid executions.
            return;
        }
        LOG.debug("update execution: {}", execution.getId());
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
        LOG.debug("update execution ... done");
    }

    /**
     * Updates only from overview.
     */
    public void updateOverview(Execution execution, JsonNode overview) {
        LoadOverview overviewLoader = new LoadOverview();
        overviewLoader.load(execution, overview);
    }

    /**
     * Updates only from execution data (debug data).
     */
    public void updateExecution(Execution execution, Statements statements) {
        execution.setDebugData(new DebugData(statements, execution));
    }

    public Execution createExecution(
            Collection<Statement> pipeline, List<MultipartFile> inputs)
            throws MonitorException {

        // TODO Move into ExecutionFactory.

        String uuid = this.createExecutionGuid();
        File directory = new File(configuration.getWorkingDirectory(), uuid);

        // Save pipeline definition.
        File definitionFile = new File(
                directory, "definition" + File.separator + "definition.trig");
        definitionFile.getParentFile().mkdirs();
        try (OutputStream stream = new FileOutputStream(definitionFile)) {
            Rio.write(pipeline, stream, RDFFormat.TRIG);
        } catch (IOException | IllegalStateException ex) {
            this.silentDeleteDirectory(directory);
            throw new MonitorException("Can't save pipeline definition.", ex);
        }

        // Save resources.
        File inputDirectory = new File(directory, "input");
        for (MultipartFile input : inputs) {
            File inputFile = new File(
                    inputDirectory, input.getOriginalFilename());
            inputFile.getParentFile().mkdirs();
            try {
                input.transferTo(inputFile);
            } catch (IOException | IllegalStateException ex) {
                this.silentDeleteDirectory(directory);
                throw new MonitorException("Can't prepare inputs.", ex);
            }
        }

        Execution execution = loadExecutionForFirstTime(directory);
        if (execution == null) {
            throw new MonitorException("Can't load new execution");
        }
        return execution;
    }

    private String createExecutionGuid() {
        return (new Date()).getTime() + "-" +
                Integer.toString(executions.size()) + "-" +
                UUID.randomUUID().toString();
    }

    private void silentDeleteDirectory(File directory) {
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
        execution.setTimeToLive(this.getNowShiftedBySeconds(5 * 60));
        // Clear statements that we do not need any more.
        execution.setPipelineStatements(Collections.emptyList());
        updateOverview(
                execution,
                DeletedOverviewFactory.create(execution, new Date()));
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
        if (execution.getStatus() == ExecutionStatus.DELETED ||
                execution.getStatus() == ExecutionStatus.INVALID) {
            return false;
        }
        return !execution.isHasFinalData();
    }

    private void deleteTombstones(Date time) {
        Collection<Execution> toDelete = new ArrayList<>(2);
        for (Execution execution : this.executions) {
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
        for (File directory : this.directoriesToDelete) {
            try {
                FileUtils.deleteDirectory(directory);
                toRemove.add(directory);
            } catch (IOException ex) {
                // The directory can be used by other process or user,
                // so it may take more attempts to delete the directory.
            }
        }
        this.directoriesToDelete.removeAll(toRemove);
    }

}
