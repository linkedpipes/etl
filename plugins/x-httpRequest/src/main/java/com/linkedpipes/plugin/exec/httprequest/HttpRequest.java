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
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.model.TripleWriter;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.eclipse.rdf4j.repository.Repository;

import java.io.File;
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
        tasks = loadTasks();
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

    private List<HttpRequestTask> loadTasks() throws LpException {
        RdfSource source = Rdf4jSource.wrapRepository(taskRdf.getRepository());
        try {
            return RdfUtils.loadList(source,
                    taskRdf.getReadGraph().stringValue(),
                    RdfToPojo.descriptorFactory(), HttpRequestTask.class);
        } catch (RdfUtilsException ex) {
            throw exceptionFactory.failure("Can't load tasks.", ex);
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
        String graph = reportRdf.getWriteGraph().stringValue();
        Repository repository = reportRdf.getRepository();
        TripleWriter writer =
                Rdf4jSource.wrapRepository(repository).getTripleWriter(graph);
        return ReportWriter.create(writer);
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
