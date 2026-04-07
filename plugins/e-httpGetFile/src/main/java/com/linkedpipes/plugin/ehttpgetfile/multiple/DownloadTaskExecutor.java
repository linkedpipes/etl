package com.linkedpipes.plugin.ehttpgetfile.multiple;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.plugin.ehttpgetfile.Downloader;
import com.linkedpipes.plugin.ehttpgetfile.DownloaderRequest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class DownloadTaskExecutor implements TaskConsumer<DownloadTask> {

    private final HttpGetFilesConfiguration configuration;

    private final WritableFilesDataUnit output;

    private final HttpRequestReport requestReport;

    public DownloadTaskExecutor(
            HttpGetFilesConfiguration configuration,
            WritableFilesDataUnit output,
            StatementsConsumer statementsConsumer,
            ReportWriter reportWriter) {
        this.configuration = configuration;
        this.output = output;
        this.requestReport = new HttpRequestReport(
                statementsConsumer, reportWriter);
    }

    @Override
    public void accept(DownloadTask task) throws LpException {
        // Validate task.
        String uri = nullForEmpty(task.getUri());
        String fileName = nullForEmpty(task.getFileName());
        if (uri == null || fileName == null) {
            throw new LpException("Invalid task definition.");
        }
        //
        File targetFile = output.createFile(task.getFileName());
        try {
            (new Downloader()).download(
                    new DownloaderRequest(
                            getHeader(task),
                            getTimeOut(task),
                            configuration.isManualFollowRedirect(),
                            configuration.isDetailLogging(),
                            configuration.isEncodeUrl(),
                            configuration.isUtf8Redirect()
                    ),
                    task.getUri(), targetFile,
                    connection -> {
                        requestReport.setTask(task);
                        this.requestReport.reportHeaderResponse(connection);
                    });
        } catch (Exception ex) {
            throw new LpException("Can't download file.", ex);
        }
    }

    private String nullForEmpty(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        return string;
    }

    private Map<String, String> getHeader(DownloadTask task) {
        Map<String, String> headers = new HashMap<>();
        for (RequestHeader header : configuration.getHeaders()) {
            headers.put(header.getKey(), header.getValue());
        }
        for (RequestHeader header : task.getHeaders()) {
            headers.put(header.getKey(), header.getValue());
        }
        return headers;
    }

    private Integer getTimeOut(DownloadTask task) {
        Integer timeOut = task.getTimeOut();
        if (timeOut == null) {
            return configuration.getTimeout();
        } else {
            return timeOut;
        }
    }

}
