package com.linkedpipes.plugin.exec.httprequest;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HttpRequest implements Component, SequentialExecution {

    @Component.InputPort(iri = "Task")
    public SingleGraphDataUnit taskRdf;

    @Component.InputPort(iri = "FilesInput")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "FilesOutput")
    public WritableFilesDataUnit outputFiles;

    @Component.InputPort(iri = "ReportRdf")
    public WritableSingleGraphDataUnit reportRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        Map<String, File> inputFilesMap = initializeInputFilesMap();
        List<HttpRequestTask> tasks = loadTasks();
        executeTasks(tasks, inputFilesMap);
    }

    private Map<String, File> initializeInputFilesMap() {
        Map<String, File> inputFilesMap = new HashMap<>();
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

    private void executeTasks(
            List<HttpRequestTask> tasks, Map<String, File> inputFilesMap)
            throws LpException {
        TaskExecutor executor = new TaskExecutor(
                exceptionFactory, outputFiles, inputFilesMap,
                new StatementsConsumer(reportRdf));
        progressReport.start(tasks.size());
        for (HttpRequestTask task : tasks) {
            try {
                executor.execute(task);
            } catch (LpException ex) {
                throw ex;
            } catch (Exception ex) {
                throw exceptionFactory.failure("Can't execute task.", ex);
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

}
