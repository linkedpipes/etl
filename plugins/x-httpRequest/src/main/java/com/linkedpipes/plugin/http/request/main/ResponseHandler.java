package com.linkedpipes.plugin.http.request.main;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.eclipse.rdf4j.model.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ResponseHandler {

    private final WritableFilesDataUnit fileWriter;

    private final HttpRequestTask task;

    private final HeaderWriter headerWriter;

    public ResponseHandler(
            WritableFilesDataUnit outputFile,
            StatementsConsumer statementsWriter,
            HttpRequestTask task,
            Resource reportResource) {
        this.fileWriter = outputFile;
        this.task = task;
        this.headerWriter = new HeaderWriter(
                statementsWriter, task, reportResource);
    }

    public void apply(HttpResponse response) throws LpException {
        StatusLine statusLine = response.getStatusLine();
        if (hasRequestFailed(statusLine)) {
            headerWriter.write(response);
            throw new LpException("Request failed '{}' : '{}'",
                    statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        if (task.isOutputHeaders()) {
            headerWriter.write(response);
        }
        writeResponseToFile(response);
    }

    private boolean hasRequestFailed(StatusLine statusLine) {
        int code = statusLine.getStatusCode();
        boolean successful = 199 < code && code < 300;
        return !successful;
    }

    private void writeResponseToFile(HttpResponse response) throws LpException {
        String fileName = task.getOutputFileName();
        if (fileName == null || fileName.isEmpty() || fileName.isBlank() ) {
            return;
        }
        File outputFile = fileWriter.createFile(fileName);
        try (InputStream stream = response.getEntity().getContent()) {
            FileUtils.copyToFile(stream, outputFile);
        } catch (IOException ex) {
            throw new LpException("Can't save content to file.", ex);
        }
    }

}
