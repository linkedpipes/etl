package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
class ExecutionStorage {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutionStorage.class);

    private final Configuration configuration;

    /**
     * Keeps active executions.
     */
    private final List<Execution> executions = new ArrayList<>(64);

    /**
     * Store directories that need to be deleted.
     */
    private final List<File> directoriesToDelete = new ArrayList<>(16);

    public ExecutionStorage(Configuration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void onInit() {
        Date updateTime = new Date();
        MonitorStatements monitorInformation = new MonitorStatements();
        File rootDirectory = this.configuration.getWorkingDirectory();
        for (File directory : rootDirectory.listFiles()) {
            if (!directory.isDirectory()) {
                continue;
            }
            Execution execution;
            try {
                execution = loadFromDirectory(directory);
            } catch (Exception ex) {
                LOG.error("Can't load execution from: {}", directory, ex);
                continue;
            }
            // All running pipelines are considered to be DANGLING as
            // we do not know if their executors are running or not.
            if (execution.getStatus() == ExecutionStatus.RUNNING) {
                execution.setStatus(ExecutionStatus.DANGLING);
                monitorInformation.update(execution);
            }
            execution.setLastCheck(updateTime);
            this.executions.add(execution);

        }
    }

    /**
     * Load execution from given directory and return it.
     *
     * @param directory
     * @return Null if the directory does not represent a valid execution.
     */
    private Execution loadFromDirectory(File directory)
            throws MonitorException {
        Execution execution = new Execution();
        execution.setIri(getExecutionIri(directory));
        execution.setDirectory(directory);

        PipelineLoader pipelineLoader = new PipelineLoader(execution);
        pipelineLoader.loadPipelineIntoExecution();

        OverviewLoader overviewLoader = new OverviewLoader();
        overviewLoader.loadFromDirectory(execution);

        // There is no execution file for queued executions.
        if (!ExecutionStatus.QUEUED.equals(execution.getStatus())) {
            ExecutionLoader executionLoader = new ExecutionLoader();
            executionLoader.loadFromDirectory(execution);
        }

        return execution;
    }

    private String getExecutionIri(File directory) {
        return this.configuration.getExecutionPrefix() + directory.getName();
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

    /**
     * Check of the execution status from a directory.
     *
     * @param execution
     */
    public void checkExecutionFromDirectory(Execution execution)
            throws MonitorException {
        switch (execution.getStatus()) {
            case FINISHED:
            case FAILED:
            case DELETED:
                // Do nothing here.
                break;
            default:
                updateFromDirectory(execution);
                break;
        }
    }

    private void updateFromDirectory(Execution execution)
            throws MonitorException {
        OverviewLoader overviewLoader = new OverviewLoader();
        overviewLoader.loadFromDirectory(execution);
        if (ExecutionStatus.QUEUED == execution.getStatus()) {
            // Queued executions do not have execution information ready.
            return;
        }
        ExecutionLoader executionLoader = new ExecutionLoader();
        executionLoader.loadFromDirectory(execution);
    }

    public void updateFromOverview(Execution execution, InputStream stream)
            throws MonitorException {
        OverviewLoader overviewLoader = new OverviewLoader();
        overviewLoader.loadFromStream(execution, stream);
    }

    /**
     * Discover and return execution instance for execution content in the
     * given stream.
     * Use to determine the execution based only on the RDF content.
     */
    public Execution discover(InputStream stream) throws MonitorException {
        Execution discovered = new Execution();
        OverviewLoader overviewLoader = new OverviewLoader();
        try {
            overviewLoader.loadFromStream(discovered, stream);
        } catch (MonitorException ex) {
            throw new MonitorException("Can't load execution.", ex);
        }
        Execution execution = this.findLocalExecution(discovered.getIri());
        if (execution == null) {
            return null;
        }
        this.updateDiscoveredExecution(discovered, execution);
        return execution;
    }

    private Execution findLocalExecution(String iri) {
        for (Execution execution : this.executions) {
            if (execution.getIri().equals(iri)) {
                return execution;
            }
        }
        return null;
    }

    private void updateDiscoveredExecution(
            Execution discovered, Execution local) {
        local.setOverviewStatements(
                discovered.getOverviewStatements());
        local.setLastExecutionChange(discovered.getLastExecutionChange());
        local.setLastCheck(discovered.getLastCheck());
        // We can change status from queue to running only.
        if (discovered.getStatus() == ExecutionStatus.RUNNING &&
                local.getStatus() == ExecutionStatus.QUEUED) {
            local.setStatus(ExecutionStatus.RUNNING);
        }
    }

    public Execution createExecution(
            Collection<Statement> pipeline, List<MultipartFile> inputs)
            throws MonitorException {
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

        // Create execution.
        Execution execution = this.loadFromDirectory(directory);
        execution.setIri(this.configuration.getExecutionPrefix() + uuid);
        execution.setDirectory(directory);
        execution.setStatus(ExecutionStatus.QUEUED);
        this.executions.add(execution);
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
        } catch (IOException ioex) {
            LOG.error("Can't delete directory.", ioex);
        }
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
        execution.setTimeToLive(this.getNowShiftedBySeconds(10 * 60));
        execution.setStatus(ExecutionStatus.DELETED);
        MonitorStatements monitorStatements = new MonitorStatements();
        monitorStatements.update(execution);
        // Clear statements about the pipeline as a tombstone
        // does not need them.
        execution.setPipelineStatements(Collections.emptyList());
        execution.setOverviewStatements(Collections.emptyList());
        this.directoriesToDelete.add(execution.getDirectory());
    }

    @Scheduled(fixedDelay = 15000, initialDelay = 200)
    public void updateExecutionsFromDirectories() {
        Date now = new Date();
        // Update only such execution that were not updated in last
        // 10 seconds. As the execution could have been updated
        // from a REST service and we do not want to load it twice.
        Date requiredLastUpdate = this.getNowShiftedBySeconds(-10);
        for (Execution execution : executions) {
            if (!requiredLastUpdate.after(execution.getLastCheck())) {
                continue;
            }
            try {
                checkExecutionFromDirectory(execution);
            } catch (MonitorException ex) {
                LOG.error("Can't execute update from a directory for: {}",
                        execution.getId(), ex);
            }
        }
        this.deleteTombstones(now);
        this.deleteDirectories();
    }

    private Date getNowShiftedBySeconds(int secondsChange) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, secondsChange);
        return now;
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
