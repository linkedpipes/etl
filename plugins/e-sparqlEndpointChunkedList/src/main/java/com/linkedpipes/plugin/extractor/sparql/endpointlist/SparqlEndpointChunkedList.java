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

import java.io.File;
import java.util.*;
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

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

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

    private Map<String, List<File>> inputFilesByName;

    @Override
    public void execute() throws LpException {
        initializeTaskLoader();
        initializeInputFileMap();
        initializeExecutor();
        initializeConsumers();
        progressReport.start(taskLoader.getTasks().size());
        executeTasks();
        awaitExecutorShutdown();
        progressReport.done();
    }

    private void initializeTaskLoader() throws LpException {
        taskLoader = new TaskLoader(tasksRdf, exceptionFactory);
        taskLoader.initialize();
    }

    private void initializeInputFileMap() {
        inputFilesByName = new HashMap<>();
        for (FilesDataUnit.Entry entry : inputFiles) {
            getOrCreate(inputFilesByName, entry.getFileName())
                    .add(entry.toFile());
        }
    }

    private <T> List<T> getOrCreate(Map<String, List<T>> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        List<T> newList = new LinkedList<>();
        map.put(key, newList);
        return newList;
    }

    private void initializeExecutor() {
        LOG.info("Using {} executors", configuration.getUsedThreads());
        executorService = new ThreadPoolExecutor(
                configuration.getUsedThreads(),
                configuration.getUsedThreads(),
                1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>());
    }

    private void executeTasks() throws LpException {
        for (Map.Entry<String, List<Task>> entry
                : groupTaskByEndpoint(taskLoader.getTasks()).entrySet()) {
            LOG.info("Submitting {} task for {}", entry.getValue().size(),
                    entry.getKey());
            submitTasks(entry.getValue());
        }
        LOG.info("executorService.shutdown");
        executorService.shutdown();
    }

    private void initializeConsumers() {
        errorConsumer = new ErrorReportConsumer(errorOutputRdf);
        resultConsumer = new TaskResultConsumer(outputRdf);
    }

    private void awaitExecutorShutdown() {
        LOG.info("executorService.awaitTermination");
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

    private Map<String, List<Task>> groupTaskByEndpoint(
            Collection<Task> tasks) {
        Map<String, List<Task>> endpointMap = new HashMap<>();
        for (Task task : tasks) {
            getOrCreate(endpointMap, task.getEndpoint()).add(task);
        }
        return endpointMap;
    }

    private void submitTasks(List<Task> tasks) throws LpException {
        waitForSpaceInQueue();
        executorService.submit(new TaskExecutor(tasks,
                errorConsumer, resultConsumer, progressReport,
                exceptionFactory, configuration.getExecutionTimeLimit(),
                inputFilesByName));
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

}
