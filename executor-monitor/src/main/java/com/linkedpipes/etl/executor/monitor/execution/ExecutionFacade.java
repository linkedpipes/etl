package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.MemoryMonitor;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class ExecutionFacade {

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecutionFacade.class);

    /**
     * General exception form used to report failures.
     */
    public static class OperationFailed extends Exception {

        public OperationFailed(String message) {
            super(message);
        }

        public OperationFailed(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public static class UnknownExecution extends Exception {

    }

    public static class ExecutionMismatch extends Exception {

        public ExecutionMismatch(String message) {
            super(message);
        }

    }

    @Autowired
    private ExecutionStorage storage;

    /**
     * @param id
     * @return
     */
    public Execution getExecution(String id) {
        return storage.getExecution(id);
    }

    /**
     * @return List of all executions.
     */
    public Collection<Execution> getExecutions() {
        return storage.getExecutions();
    }

    /**
     * Incremental checkExecution from the given time.
     *
     * @param changedSince
     * @return
     */
    public Collection<Execution> getExecutions(Date changedSince) {
        final Collection<Execution> executions = storage.getExecutions();
        final ArrayList<Execution> result = new ArrayList<>(4);
        for (Execution execution : executions) {
            if (execution.changedAfter(changedSince)) {
                result.add(execution);
            }
        }
        return result;
    }

    /**
     * Executions with status {@link Execution.StatusType#QUEUED}..
     *
     * @return List of executions, in order in which they should be executed.
     */
    public Collection<Execution> getExecutionsQueued() {
        final List<Execution> result = new LinkedList<>();
        for (Execution execution : storage.getExecutions()) {
            if (execution.getStatus() == Execution.StatusType.QUEUED) {
                result.add(execution);
            }
        }
        result.sort(Comparator.comparing(Execution::getId));
        return result;
    }

    /**
     * @param execution
     * @return Path to the execution log file.
     */
    public File getExecutionLogFile(Execution execution) {
        return new File(execution.getDirectory(), "log/execution.log");
    }

    /**
     * Write statements about given execution into given stream.
     *
     * @param execution
     * @param format
     * @param stream
     */
    public void writeExecution(Execution execution, RDFFormat format,
            OutputStream stream) throws OperationFailed, UnknownExecution {
        if (execution == null) {
            throw new UnknownExecution();
        }
        final List<Statement> statements
                = execution.getExecutionStatementsFull();
        if (statements == null) {
            // Stream content from a file.
            final File executionFile
                    = new File(execution.getDirectory(), "execution.jsonld");
            streamFile(executionFile, RDFFormat.JSONLD, format, stream);
        } else {
            // Serialize from the memory.
            final RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();
            for (Statement statement : statements) {
                writer.handleStatement(statement);
            }
            writer.endRDF();
        }

    }

    /**
     * Write statements about given execution's pipeline into given stream.
     *
     * @param execution
     * @param format
     * @param stream
     */
    public void writePipeline(Execution execution, RDFFormat format,
            OutputStream stream) throws OperationFailed, UnknownExecution {
        if (execution == null) {
            throw new UnknownExecution();
        }
        final File pipelineFile
                = new File(execution.getDirectory(), "pipeline.jsonld");
        streamFile(pipelineFile, RDFFormat.JSONLD, format, stream);
    }

    public void writeOverview(Execution execution,
            OutputStream stream) throws IOException {
        execution.getOverviewResource().writeToStream(stream);
    }

    /**
     * Create execution from given sources.
     *
     * @param pipeline
     * @param inputs
     * @return
     */
    public Execution createExecution(MultipartFile pipeline,
            List<MultipartFile> inputs) throws OperationFailed {
        return storage.createExecution(pipeline, inputs);
    }

    /**
     * Delete given execution.
     *
     * @param execution
     */
    public void deleteExecution(Execution execution) {
        storage.delete(execution);
    }

    /**
     * Parse execution in given stream and match it with existing execution.
     * If no execution with given IRI is not find then throws an exception.
     *
     * @param stream
     * @return
     */
    public Execution discover(InputStream stream)
            throws UnknownExecution, OperationFailed {
        return storage.discover(stream);
    }

    /**
     * Update given execution from given stream.
     *
     * Check based on execution IRI that the execution match to given
     * stream. In case of mismatch throws an exception. In such case
     * the {@link #discover(java.io.InputStream)} should be called.
     *
     * @param execution
     * @param stream
     */
    public void update(Execution execution, InputStream stream)
            throws ExecutionMismatch, OperationFailed {
        MemoryMonitor.log(LOG, "update.before");
        storage.checkExecution(execution, stream);
        MemoryMonitor.log(LOG, "update.after");
    }

    /**
     * Force execution load from a directory.
     *
     * @param execution
     */
    public void updateFromFile(Execution execution)
            throws OperationFailed, ExecutionMismatch {
        MemoryMonitor.log(LOG, "updateFromFile.before");
        ExecutionChecker.updateFromDirectory(execution);
        MemoryMonitor.log(LOG, "updateFromFile.after");
    }

    /**
     * Must be called when an executor is assigned to the execution.
     *
     * @param execution
     */
    public void attachExecutor(Execution execution) {
        if (execution.getStatus() != Execution.StatusType.FINISHED) {
            execution.setStatus(Execution.StatusType.RUNNING);
            ExecutionChecker.updateGenerated(execution);
        }
    }

    /**
     * Must be called when executor got unresponsive.
     *
     * @param execution
     */
    public void unresponsiveExecutor(Execution execution) {
        if (execution.getStatus() != Execution.StatusType.FINISHED) {
            execution.setStatus(Execution.StatusType.UNRESPONSIVE);
            ExecutionChecker.updateGenerated(execution);
        }
    }

    /**
     * Must be called when an executor is detached from the execution. Ie.
     * if in state UNRESPONSIVE (or without executor) and all known
     * executors are executing other pipelines.
     *
     * @param execution
     */
    public void detachExecutor(Execution execution) {
        storage.checkExecution(execution);
        LOG.info(" detaching: {} {}", execution.getId(), execution.getStatus());
        if (execution.getStatus() != Execution.StatusType.FINISHED) {
            execution.setStatus(Execution.StatusType.DANGLING);
            ExecutionChecker.updateGenerated(execution);
        }
    }

    /**
     * Stream given file to given stream.
     *
     * @param file
     * @param sourceFormat
     * @param targetFormat
     * @param stream
     */
    private void streamFile(File file, RDFFormat sourceFormat,
            RDFFormat targetFormat, OutputStream stream)
            throws OperationFailed {
        if (sourceFormat.equals(targetFormat)) {
            try {
                FileUtils.copyFile(file, stream);
            } catch (IOException ex) {
                throw new OperationFailed("Can't copy file to stream.", ex);
            }
        } else {
            final RDFWriter writer = Rio.createWriter(targetFormat, stream);
            try (InputStream input = new FileInputStream(file)) {
                final RDFParser parser = Rio.createParser(sourceFormat);
                parser.setRDFHandler(writer);
                parser.parse(input, "http://localhost/base/");
            } catch (IOException ex) {
                throw new OperationFailed("Can't read file.", ex);
            }
        }
    }

}
