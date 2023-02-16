package com.linkedpipes.plugin.ehttpgetfile.multiple;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;

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
        requestReport.setTask(task);
        try {
            (new Downloader(
                    new Downloader.Configuration(
                            configuration.isManualFollowRedirect(),
                            configuration.isDetailLogging(),
                            configuration.isEncodeUrl(),
                            configuration.isUtf8Redirect())))
                    .download(createDownloaderTask(task), requestReport);
        } catch (Exception ex) {
            throw new LpException("Can't download file.", ex);
        }
    }

    private Downloader.Task createDownloaderTask(DownloadTask task)
            throws LpException {
        String uri = nullForEmpty(task.getUri());
        String fileName = nullForEmpty(task.getFileName());
        if (uri == null || fileName == null) {
            throw new LpException("Invalid task definition.");
        }
        File targetFile = output.createFile(task.getFileName());
        return new Downloader.Task(uri,
                targetFile, getHeader(task), getTimeOut(task));
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
