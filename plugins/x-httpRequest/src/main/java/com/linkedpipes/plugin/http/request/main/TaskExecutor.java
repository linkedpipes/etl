package com.linkedpipes.plugin.http.request.main;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.plugin.http.apache.RequestConfiguration;
import com.linkedpipes.plugin.http.apache.RequestExecutor;
import org.apache.http.HttpResponse;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

class TaskExecutor implements TaskConsumer<HttpRequestTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskExecutor.class);

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final WritableFilesDataUnit outputFiles;

    private final StatementsConsumer statementsWriter;

    private final Map<String, File> inputFilesMap;

    private final boolean encodeUrl;

    public TaskExecutor(
            WritableFilesDataUnit outputFiles,
            StatementsConsumer statementsWriter,
            Map<String, File> inputFilesMap,
            boolean encodeUrl) {
        this.outputFiles = outputFiles;
        this.statementsWriter = statementsWriter;
        this.inputFilesMap = inputFilesMap;
        this.encodeUrl = encodeUrl;
    }

    @Override
    public void accept(HttpRequestTask task) throws LpException {
        LOG.info("Executing '{}' on '{}' to '{}'",
                task.getMethod(),
                task.getUrl(),
                task.getOutputFileName());
        var requestConfiguration = createConfiguration(task);
        var reportResource = createReportResource(task);
        var executor = new RequestExecutor(
                requestConfiguration,
                response -> handleResponse(task, reportResource, response));
        try {
            executor.execute();
        } catch (IOException ex) {
            throw new LpException("Can't execute request.", ex);
        }
    }

    private RequestConfiguration createConfiguration(HttpRequestTask task) {
        var result = new RequestConfiguration();
        result.url = task.getUrl();
        result.method = task.getMethod();
        task.getHeaders().forEach(item -> {
            result.headers.put(item.getName(), item.getValue());
        });
        result.timeout = task.getTimeOut();
        result.contentAsBody = task.isPostContentAsBody();
        result.content = task.getContent().stream().map(item -> {
            var content = new RequestConfiguration.Content();
            content.file = inputFilesMap.get(item.getFileReference());
            content.value = item.getValue();
            content.name = item.getName();
            content.fileName = item.getFileName();
            return content;
        }).toList();
        result.encodeUrl = encodeUrl;
        return result;
    }

    private IRI createReportResource(HttpRequestTask task) {
        return valueFactory.createIRI(task.deriveIri("report"));
    }

    public void handleResponse(
            HttpRequestTask task, Resource reportResource,
            HttpResponse response) throws LpException {
        var handler = new ResponseHandler(
                outputFiles, statementsWriter, task, reportResource);
        handler.apply(response);
    }

}
