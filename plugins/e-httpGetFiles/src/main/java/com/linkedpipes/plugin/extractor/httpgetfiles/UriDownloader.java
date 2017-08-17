package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;

class UriDownloader {

    private class Worker implements Callable<Object> {

        private final ConcurrentLinkedQueue<Downloader.Task> workQueue;

        private final Map<String, String> contextMap =
                MDC.getCopyOfContextMap();

        public Worker(ConcurrentLinkedQueue<Downloader.Task> workQueue) {
            this.workQueue = workQueue;
        }

        @Override
        public Object call() {
            MDC.setContextMap(contextMap);
            Downloader.Task task;
            while ((task = workQueue.poll()) != null) {
                if (!configuration.isSkipOnError() && !exceptions.isEmpty()) {
                    return null;
                }
                downloadFile(task);
                progressReport.entryProcessed();
            }
            return null;
        }

        private void downloadFile(Downloader.Task task) {
            Downloader downloader = new Downloader(
                    configuration.isForceFollowRedirect(), task,
                    configuration.isDetailLogging());
            try {
                downloader.download();
            } catch (Exception ex) {
                LOG.error("Can't download file from: {}",
                        task.getSourceUrl(), ex);
                exceptions.add(ex);
            }
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(UriDownloader.class);

    private final ProgressReport progressReport;

    private final HttpGetFilesConfiguration configuration;

    private final ConcurrentLinkedQueue<Exception> exceptions =
            new ConcurrentLinkedQueue<>();

    public UriDownloader(ProgressReport progressReport,
            HttpGetFilesConfiguration configuration) {
        this.progressReport = progressReport;
        this.configuration = configuration;
    }

    public void download(ConcurrentLinkedQueue<Downloader.Task> toDownload,
            long size) {
        progressReport.start(size);
        //
        ExecutorService executor = Executors.newFixedThreadPool(
                configuration.getThreads());
        for (int i = 0; i < configuration.getThreads(); ++i) {
            executor.submit(new Worker(toDownload));
        }
        // Wait till all is done.
        executor.shutdown();
        while (true) {
            try {
                if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }
        //
        progressReport.done();
    }

    public Collection<Exception> getExceptions() {
        return Collections.unmodifiableCollection(exceptions);
    }

}
