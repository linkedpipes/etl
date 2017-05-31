package com.linkedpipes.plugin.exec.httprequest;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

class TaskExecutor {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskExecutor.class);

    private final ExceptionFactory exceptionFactory;

    private final WritableFilesDataUnit outputFiles;

    private final TaskContentWriter taskContentWriter;

    public TaskExecutor(
            ExceptionFactory exceptionFactory,
            WritableFilesDataUnit outputFiles,
            Map<String, File> inputFilesMap) {
        this.exceptionFactory = exceptionFactory;
        this.outputFiles = outputFiles;
        this.taskContentWriter = new TaskContentWriter(
                exceptionFactory, inputFilesMap);
    }

    public void execute(HttpRequestTask task) throws LpException {
        LOG.info("Executing '{}' on '{}' to '{}'", task.getMethod(),
                task.getUrl(), task.getOutputFileName());
        try (MultipartConnection connection = createHttpConnection(task)) {
            taskContentWriter.addTaskContent(connection, task);
            connection.finishRequest();
            checkStatus(connection);
            saveResponse(connection.getConnection(), task);
        } catch (Exception ex) {
            throw exceptionFactory.failure("Can't create connection.", ex);
        }
    }

    private MultipartConnection createHttpConnection(HttpRequestTask task)
            throws IOException {
        URL url = new URL(task.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(task.getMethod());
        for (HttpRequestTask.Header header : task.getHeaders()) {
            connection.setRequestProperty(header.getName(), header.getValue());
        }
        return new MultipartConnection(connection);
    }

    private void checkStatus(MultipartConnection connection)
            throws IOException, LpException {
        if (connection.requestFailed()) {
            throw exceptionFactory.failure("Request failed with status: {}",
                    connection.getResponseCode());
        }
    }

    private void saveResponse(HttpURLConnection connection,
            HttpRequestTask task)
            throws LpException, IOException {
        String fileName = task.getOutputFileName();
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        File outputFile = this.outputFiles.createFile(fileName);
        try (InputStream stream = connection.getInputStream()) {
            FileUtils.copyToFile(stream, outputFile);
        }
    }

}
