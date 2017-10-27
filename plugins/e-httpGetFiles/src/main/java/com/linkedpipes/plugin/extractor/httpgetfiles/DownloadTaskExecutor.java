package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class DownloadTaskExecutor implements TaskConsumer<DownloadTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(DownloadTaskExecutor.class);

    private HttpGetFilesConfiguration configuration;

    private ProgressReport progressReport;

    private WritableFilesDataUnit output;

    private ExceptionFactory exceptionFactory;

    public DownloadTaskExecutor(
            HttpGetFilesConfiguration configuration,
            ProgressReport progressReport,
            WritableFilesDataUnit output,
            ExceptionFactory exceptionFactory) {
        this.configuration = configuration;
        this.progressReport = progressReport;
        this.output = output;
        this.exceptionFactory = exceptionFactory;
    }

    @Override
    public void accept(DownloadTask task) throws LpException {
        Downloader downloader = new Downloader(
                configuration.isForceFollowRedirect(),
                createDownloaderTask(task),
                configuration.isDetailLogging());
        try {
            downloader.download();
        } catch (Exception ex) {
            LOG.error("Can't download file from: {}", task.getUri(), ex);
            throw exceptionFactory.failure("Cam't download file.", ex);
        }

    }

    private Downloader.Task createDownloaderTask(DownloadTask task)
            throws LpException {
        String uri = nullForEmpty(task.getUri());
        String fileName = nullForEmpty(task.getFileName());
        if (uri == null || fileName == null) {
            throw exceptionFactory.failure("Invalid reference.");
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
