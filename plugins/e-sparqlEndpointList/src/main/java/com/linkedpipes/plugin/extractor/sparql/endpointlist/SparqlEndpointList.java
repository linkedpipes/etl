package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SparqlEndpointList
        implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(SparqlEndpointList.class);

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.InputPort(iri = "ErrorOutputRdf")
    public WritableSingleGraphDataUnit errorOutputRdf;

    @Component.InputPort(iri = "Tasks")
    public SingleGraphDataUnit tasksRdf;

    @Component.Configuration
    public SparqlEndpointListConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    private TaskLoader taskLoader;

    private ExecutorService executorService;

    @Override
    public void execute() throws LpException {
        initializeTaskLoader();
        initializeExecutor();
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

    private void closeTaskLoader() {
        if (taskLoader != null) {
            taskLoader.close();
        }
    }

    private void executeTasks() {
        ErrorReportConsumer errorConsumer =
                new ErrorReportConsumer(errorOutputRdf);
        TaskResultConsumer resultConsumer =
                new TaskResultConsumer(outputRdf);
        LOG.info("Number of tasks {}", taskLoader.getTasks().size());
        for (Task task : taskLoader.getTasks()) {
            executorService.submit(new TaskExecutor(
                    task, errorConsumer, resultConsumer, progressReport));
            progressReport.entryProcessed();
        }
        executorService.shutdown();
    }

    private void initializeExecutor() {
        LOG.info("Using {} executors", configuration.getUsedThreads());
        executorService = Executors.newFixedThreadPool(
                configuration.getUsedThreads());
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

}
