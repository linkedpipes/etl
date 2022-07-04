package com.linkedpipes.plugin.exec.httprequest;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TaskExecutor implements TaskConsumer<HttpRequestTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaskExecutor.class);

    private final WritableFilesDataUnit outputFiles;

    private final TaskContentWriter taskContentWriter;

    private final boolean encodeUrl;

    private final HeaderReporter headerReporter;

    private final ProgressReport progressReport;

    private final Set<URL> visited = new HashSet<>();

    private HttpRequestTask task;

    public TaskExecutor(
            WritableFilesDataUnit outputFiles,
            Map<String, File> inputFilesMap,
            StatementsConsumer consumer,
            ProgressReport progressReport,
            boolean encodeUrl) {
        this.outputFiles = outputFiles;
        this.taskContentWriter = new TaskContentWriter(inputFilesMap);
        this.headerReporter = new HeaderReporter(consumer);
        this.progressReport = progressReport;
        this.encodeUrl = encodeUrl;
    }

    @Override
    public void accept(HttpRequestTask task) throws LpException {
        LOG.info("Executing '{}' on '{}' to '{}'", task.getMethod(),
                task.getUrl(), task.getOutputFileName());
        this.task = task;
        URL url = createUrl(task.getUrl());
        try {
            visited.clear();
            performRequest(url);
        } finally {
            progressReport.entryProcessed();
        }
    }

    private URL createUrl(String urlAsString) throws LpException {
        URL url;
        try {
            // Parse so we have access to parts.
            url = new URL(urlAsString);
            // Encode the host to support IDN.
            url = new URL(
                    url.getProtocol(),
                    IDN.toASCII(url.getHost()),
                    url.getPort(),
                    url.getFile());
        } catch (IOException ex) {
            throw new LpException("Can't create URL: {}", urlAsString, ex);
        }
        if (encodeUrl) {
            try {
                url = new URL(url.toURI().toASCIIString());
            } catch (IOException | URISyntaxException ex) {
                throw new LpException("Can't convert to URI: {}", url, ex);
            }
        }
        return url;
    }

    private void performRequest(URL url) throws LpException {
        LOG.debug("Creating connection {} ...", url);
        if (visited.contains(url)) {
            throw new LpException("Cycle detected.");
        }
        visited.add(url);
        try (Connection connection = createConnection(url)) {
            LOG.debug("Requesting response {} ...", url);
            connection.finishRequest();
            if (shouldFollowRedirect(connection)) {
                LOG.debug("Following redirect {} ...", url);
                handleRedirect(connection);
            } else {
                LOG.debug("Handling response {} ...", url);
                handleResponse(connection);
            }
            LOG.debug("Done {} ...", url);
        } catch (Exception ex) {
            throw new LpException("Request failed for: {}", url, ex);
        }
    }

    private boolean shouldFollowRedirect(Connection connection)
            throws IOException {
        if (!connection.requestRedirect()) {
            return false;
        }
        return task.isFollowRedirect() || task.isHasUtf8Redirect();
    }

    private Connection createConnection(URL url)
            throws IOException, LpException {
        // TODO Add support for single body request.
        HttpURLConnection connection = createHttpConnection(url);
        if (task.getContent().isEmpty()) {
            return wrapConnection(connection);
        }
        if (task.isPostContentAsBody()) {
            LOG.debug("Adding body ...");
            if (task.getContent().size() == 1) {
                return wrapPostBody(connection, task.getContent().get(0));
            } else {
                throw new LpException("POST with body "
                        + "can be used only with a single content file. "
                        + "Task: {}",
                        task.getIri());
            }
        }
        LOG.debug("Wrapping as a multipart ...");
        return wrapMultipart(connection, task);

    }

    private HttpURLConnection createHttpConnection(URL url)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (task.isHasUtf8Redirect()) {
            // We need to do this manually.
            connection.setInstanceFollowRedirects(false);
        }
        connection.setRequestMethod(task.getMethod());
        if (task.getTimeOut() != null) {
            connection.setConnectTimeout(task.getTimeOut());
            connection.setReadTimeout(task.getTimeOut());
            LOG.debug("Setting timetout to: {}", task.getTimeOut());
        }
        for (HttpRequestTask.Header header : task.getHeaders()) {
            connection.setRequestProperty(header.getName(), header.getValue());
        }
        return connection;
    }


    private Connection wrapConnection(HttpURLConnection connection) {
        return new Connection(connection);
    }

    private Connection wrapPostBody(
            HttpURLConnection connection, HttpRequestTask.Content content)
            throws IOException {
        connection.setDoOutput(true);
        taskContentWriter.writeContentToConnection(connection, content);
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
        String location = connection.getResponseHeader("Location");
        if (task.isHasUtf8Redirect()) {
            location = new String(
                    location.getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);
        }
        URL urlToFollow;
        if (location.startsWith("/")) {
            // Resolve relative path.
            urlToFollow = new URL(connection.getConnection().getURL(), location);
        } else {
            urlToFollow = new URL(location);
        }
        connection.close();
        performRequest(urlToFollow);
    }

    private void handleResponse(Connection connection)
            throws IOException, LpException {
        HttpURLConnection urlConnection = connection.getConnection();
        if (connection.requestFailed()) {
            headerReporter.reportHeaderResponse(urlConnection, task);
            throw new LpException(
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
