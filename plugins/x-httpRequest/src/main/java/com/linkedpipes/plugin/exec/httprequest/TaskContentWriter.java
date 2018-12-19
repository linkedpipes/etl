package com.linkedpipes.plugin.exec.httprequest;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

class TaskContentWriter {

    private final ExceptionFactory exceptionFactory;

    private final Map<String, File> inputFilesMap;

    private MultipartConnection connection;

    public TaskContentWriter(ExceptionFactory exceptionFactory,
                             Map<String, File> inputFilesMap) {
        this.exceptionFactory = exceptionFactory;
        this.inputFilesMap = inputFilesMap;
    }

    public void addTaskContent(MultipartConnection connection,
                               HttpRequestTask task) throws LpException {
        this.connection = connection;
        for (HttpRequestTask.Content content : task.getContent()) {
            addContentToConnection(content);
        }
    }

    private void addContentToConnection(HttpRequestTask.Content content)
            throws LpException {
        String fileName = content.getFileName();
        if (fileName != null && !fileName.isEmpty()) {
            addFileToConnection(content.getName(), content.getFileName(),
                    content.getFileReference());
        } else {
            String value = content.getValue();
            if (value != null) {
                addFieldToConnection(content.getName(), content.getValue());
            }
        }
    }

    private void addFileToConnection(
            String name, String fileName, String fileReference)
            throws LpException {
        File file = inputFilesMap.get(fileReference);
        if (file == null) {
            throw exceptionFactory.failure("Missing file: {}", fileName);
        } else {
            try {
                connection.addFile(name, fileName, file);
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't add file content.", ex);
            }
        }
    }

    private void addFieldToConnection(String name, String value) {
        connection.addField(name, value);
    }

    public void writeContentToConnection(
            HttpURLConnection connection,
            HttpRequestTask.Content content) throws IOException {
        File file = inputFilesMap.get(content.getFileReference());
        connection.setRequestProperty(
                "Content-Length", Long.toString(file.length()));
        FileUtils.copyFile(file, connection.getOutputStream());
    }

}
