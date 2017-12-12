package com.linkedpipes.plugin.exec.httprequest;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecution;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HttpRequest extends TaskExecution<HttpRequestTask> {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "Task")
    public SingleGraphDataUnit taskRdf;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.InputPort(iri = "ReportRdf")
    public WritableSingleGraphDataUnit reportRdf;

    @Component.Configuration
    public HttpRequestConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    private Map<String, File> inputFilesMap;

    private List<HttpRequestTask> tasks;

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        return this.configuration;
    }

    @Override
    protected TaskSource<HttpRequestTask> createTaskSource()
            throws LpException {
        initializeInputFilesMap();
        loadTasks();
        propagateConfigurationToTask();
        TaskSource<HttpRequestTask> source = TaskSource.groupTaskSource(
                this.tasks, configuration.getThreadsPerGroup());
        source.setSkipOnError(configuration.isSkipOnError());
        return source;
    }

    private Map<String, File> initializeInputFilesMap() {
        inputFilesMap = new HashMap<>();
        for (FilesDataUnit.Entry entry : inputFiles) {
            inputFilesMap.put(entry.getFileName(), entry.toFile());
        }
        return inputFilesMap;
    }

    private void loadTasks() throws LpException {
        RdfSource source = taskRdf.asRdfSource();
        List<String> resources = source.getByType(
                HttpRequestVocabulary.TASK);
        tasks = new ArrayList<>(resources.size());
        for (String resource : resources) {
            HttpRequestTask task = new HttpRequestTask();
            RdfToPojoLoader.loadByReflection(source, resource, task);
            tasks.add(task);
        }
    }

    private void propagateConfigurationToTask() {
        for (HttpRequestTask task : tasks) {
            if (task.isFollowRedirect() == null) {
                task.setFollowRedirect(configuration.isFollowRedirect());
            }
        }
    }

    @Override
    protected TaskConsumer<HttpRequestTask> createConsumer() {
        return new TaskExecutor(
                exceptionFactory, outputFiles, inputFilesMap,
                new StatementsConsumer(reportRdf), progressReport);
    }

    @Override
    protected ReportWriter createReportWriter() {
        return ReportWriter.create(reportRdf.getWriter());
    }

    @Override
    protected void beforeExecution() throws LpException {
        super.beforeExecution();
        this.progressReport.start(tasks);
    }

    @Override
    protected void afterExecution() throws LpException {
        super.afterExecution();
        this.progressReport.done();
    }

}
