package com.linkedpipes.plugin.exec.httprequest;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

class TaskExecutor implements TaskConsumer<HttpRequestTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskExecutor.class);

    private final ExceptionFactory exceptionFactory;

    private final WritableFilesDataUnit outputFiles;

    private final TaskContentWriter taskContentWriter;

    private HeaderReporter headerReporter;

    private ProgressReport progressReport;

    private HttpRequestTask task;

    public TaskExecutor(
            ExceptionFactory exceptionFactory,
            WritableFilesDataUnit outputFiles,
            Map<String, File> inputFilesMap,
            StatementsConsumer consumer,
            ProgressReport progressReport) {
        this.exceptionFactory = exceptionFactory;
        this.outputFiles = outputFiles;
        this.taskContentWriter = new TaskContentWriter(
                exceptionFactory, inputFilesMap);
        this.headerReporter = new HeaderReporter(consumer);
        this.progressReport = progressReport;
    }

    @Override
    public void accept(HttpRequestTask task) throws LpException {
        LOG.info("Executing '{}' on '{}' to '{}'", task.getMethod(),
                task.getUrl(), task.getOutputFileName());
        this.task = task;
        URL url = createUrl(task.getUrl());
        try {
            performRequest(url);
        } finally {
            progressReport.entryProcessed();
        }
    }

    private URL createUrl(String url) throws LpException {
        try {
            return new URL(task.getUrl());
        } catch (IOException ex) {
            throw new LpException("Invalid URL: {}", url);
        }
    }

    private void performRequest(URL url) throws LpException {
        try (Connection connection = createConnection(url)) {
            connection.finishRequest();
            if (shouldFollowRedirect(connection)) {
                handleRedirect(connection);
            } else {
                handleResponse(connection);
            }
        } catch (Exception ex) {
            throw exceptionFactory.failure("Request failed for: {}", url, ex);
        }
    }

    private boolean shouldFollowRedirect(Connection connection)
            throws IOException {
        if (task.isFollowRedirect() == null) {
            return false;
        }
        return task.isFollowRedirect() && connection.requestRedirect();
    }

    private Connection createConnection(URL url)
            throws IOException, LpException {
        // TODO Add support for single body request.
        HttpURLConnection connection = createHttpConnection(url);
        if (task.getContent().isEmpty()) {
            return wrapConnection(connection);
        } else {
            return wrapMultipart(connection, task);
        }
    }

    private HttpURLConnection createHttpConnection(URL url)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(task.getMethod());
        for (HttpRequestTask.Header header : task.getHeaders()) {
            connection.setRequestProperty(header.getName(), header.getValue());
        }
        return connection;
    }

    private Connection wrapConnection(HttpURLConnection connection) {
        return new Connection(connection);
    }

    private Connection wrapMultipart(
            HttpURLConnection connection, HttpRequestTask task)
            throws IOException, LpException {
        MultipartConnection multipartConnection =
                new MultipartConnection(connection);
        taskContentWriter.addTaskContent(multipartConnection, task);
        return multipartConnection;
    }

    private void handleRedirect(Connection connection)
            throws Exception {
        URL urlToFollow = new URL(connection.getResponseHeader("Location"));
        connection.close();
        performRequest(urlToFollow);
    }

    private void handleResponse(Connection connection)
            throws IOException, LpException {
        HttpURLConnection urlConnection = connection.getConnection();
        if (connection.requestFailed()) {
            headerReporter.reportHeaderResponse(urlConnection, task);
            throw exceptionFactory.failure(
                    "{} : {}",
                    connection.getResponseCode(),
                    connection.getResponseMessage());
        }
        if (task.isOutputHeaders()) {
            headerReporter.reportHeaderResponse(urlConnection, task);
        }
        saveFileResponse(urlConnection, task);
    }

    private void saveFileResponse(
            HttpURLConnection connection, HttpRequestTask task)
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
