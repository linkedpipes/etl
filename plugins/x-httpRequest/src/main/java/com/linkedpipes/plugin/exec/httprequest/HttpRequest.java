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
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;

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

    private Map<String, File> inputFilesMap;

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        TaskExecutionConfiguration result = new TaskExecutionConfiguration();
        result.numberOfThreads = configuration.getThreadsNumber();
        result.numberOfThreadsPerGroup =  configuration.getThreadsPerGroup();
        result.skipFailedTasks = configuration.isSkipOnError();
        return result;
    }

    @Override
    protected List<HttpRequestTask> loadTasks() throws LpException {
        RdfSource source = taskRdf.asRdfSource();
        List<String> resources = source.getByType(
                HttpRequestVocabulary.TASK);
        List<HttpRequestTask>  result = new ArrayList<>(resources.size());
        for (String resource : resources) {
            HttpRequestTask task = new HttpRequestTask();
            RdfToPojoLoader.loadByReflection(source, resource, task);
            result.add(task);
        }
        propagateConfigurationToTask(result);
        return result;
    }

    private void propagateConfigurationToTask(List<HttpRequestTask>  tasks) {
        for (HttpRequestTask task : tasks) {
            if (task.isFollowRedirect() == null) {
                task.setFollowRedirect(configuration.isFollowRedirect());
            }
            if (task.isHasUtf8Redirect() == null) {
                task.setHasUtf8Redirect(configuration.isHasUtf8Redirect());
            }
            if (task.getTimeOut() == null) {
                task.setTimeOut(configuration.getTimeOut());
            }
        }
    }

    @Override
    protected ReportWriter createReportWriter() {
        return ReportWriter.create(reportRdf.getWriter());
    }

    @Override
    protected TaskConsumer<HttpRequestTask> createConsumer() {
        return new TaskExecutor(
                outputFiles, inputFilesMap,
                new StatementsConsumer(reportRdf),
                configuration.isEncodeUrl());
    }

    @Override
    protected void onInitialize(Context context) throws LpException {
        super.onInitialize(context);
        initializeInputFilesMap();
    }

    private void initializeInputFilesMap() {
        inputFilesMap = new HashMap<>();
        for (FilesDataUnit.Entry entry : inputFiles) {
            inputFilesMap.put(entry.getFileName(), entry.toFile());
        }
    }

}
