package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableChunkedTriples;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Takes CSV files on input. The CSV file rows are used as IRIs and mapped
 * to the given SPARQL as the ${VALUES} placeholder.
 *
 * Example query:
 * CONSTRUCT { ?obec ?p ?o } WHERE { ?obec ?p ?o ${VALUES} }
 * where the input CSV file contains column "obec".
 */
public final class SparqlEndpointChunkedList implements Component,
        SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(SparqlEndpointChunkedList.class);

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputRdf")
    public WritableChunkedTriples outputRdf;

    @Component.InputPort(iri = "ErrorOutputRdf")
    public WritableSingleGraphDataUnit errorOutputRdf;

    @Component.InputPort(iri = "Tasks")
    public SingleGraphDataUnit tasksRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Configuration
    public SparqlEndpointChunkedListConfiguration configuration;

    private TaskLoader taskLoader;

    private ThreadPoolExecutor executorService;

    private ErrorReportConsumer errorConsumer;

    private TaskResultConsumer resultConsumer;

    @Override
    public void execute() throws LpException {
        initializeTaskLoader();
        initializeExecutor();
        initializeConsumers();
        try {
            progressReport.start(taskLoader.getTasks().size());
            executeTasks();
            awaitExecutorShutdown();
            progressReport.done();
        } finally {
            closeTaskLoader();
        }
    }

    private void initializeTaskLoader() throws LpException {
        taskLoader = new TaskLoader(tasksRdf, exceptionFactory);
        taskLoader.initialize();
    }

    private void initializeConsumers() {
        errorConsumer = new ErrorReportConsumer(errorOutputRdf);
        resultConsumer = new TaskResultConsumer(outputRdf);
    }

    private void initializeExecutor() {
        LOG.info("Using {} executors", configuration.getUsedThreads());
        executorService = new ThreadPoolExecutor(
                1, configuration.getUsedThreads(),
                1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>());
    }

    private void executeTasks() throws LpException {
        LOG.info("Number of tasks: {}", taskLoader.getTasks().size());
        LOG.info("Number of files: {}", inputFiles.size());
        for (Task task : taskLoader.getTasks()) {
            submitTask(task);
        }
        executorService.shutdown();
    }

    private void awaitExecutorShutdown() {
        while (true) {
            try {
                if (executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }
    }

    private void submitTask(Task task) throws LpException {
        String fileNameFilter = task.getFileName();
        for (FilesDataUnit.Entry entry : inputFiles) {
            if (!entry.getFileName().equals(fileNameFilter)) {
                continue;
            }
            waitForSpaceInQueue();
            // For now there is just one file for task, in case of multiple
            // files we still need to use a single task - ie. to have
            // at most one thread working with given endpoint.
            //
            // TODO May cause concurrent reading of a single file.
            //
            Task taskInstance = createTask(task, entry);
            executorService.submit(new TaskExecutor(taskInstance,
                    errorConsumer, resultConsumer, progressReport,
                    exceptionFactory, configuration.getExecutionTimeLimit()));
        }
    }

    private Task createTask(Task task, FilesDataUnit.Entry entry)
            throws LpException {
        try {
            return new Task(task, entry.toFile());
        } catch (UnsupportedEncodingException ex) {
            throw new LpException("Unsupported encoding exception.", ex);
        }
    }

    private void waitForSpaceInQueue() {
        int maxQueueSize = configuration.getUsedThreads() * 2;
        while (executorService.getQueue().size() > maxQueueSize) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                // Do nothing here.
            }
        }
    }

    private void closeTaskLoader() {
        if (taskLoader != null) {
            taskLoader.close();
        }
    }

}
