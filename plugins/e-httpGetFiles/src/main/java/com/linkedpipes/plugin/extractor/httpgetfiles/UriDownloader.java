package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class UriDownloader {

    /**
     * Represent a file to download.
     */
    public static class FileToDownload {

        private final URL source;

        private final File target;

        private Map<String, String> headers = new HashMap<>();

        public FileToDownload(URL source, File target) {
            this.source = source;
            this.target = target;
        }

        public void setHeader(String key, String value) {
            headers.put(key, value);
        }
    }

    private class Worker implements Callable<Object> {

        private final ConcurrentLinkedQueue<FileToDownload> workQueue;

        private final AtomicInteger counter;

        public Worker(
                ConcurrentLinkedQueue<FileToDownload> workQueue,
                AtomicInteger counter) {
            this.workQueue = workQueue;
            this.counter = counter;
        }

        @Override
        public Object call() throws Exception {
            FileToDownload work;
            while ((work = workQueue.poll()) != null) {
                final Date downloadStarted = new Date();
                LOG.debug("Downloading {}/{} : {}",
                        counter.incrementAndGet(), total, work.source);
                // Check failure of other threads.
                if (!configuration.isSkipOnError() &&
                        !exceptions.isEmpty()) {
                    return null;
                }
                // Create connection.
                final HttpURLConnection connection;
                try {
                    connection = createConnection(work.source, work.headers);
                } catch (IOException ex) {
                    LOG.error("Can't create connection to: {}", ex);
                    exceptions.add(ex);
                    progressReport.entryProcessed();
                    continue;
                }
                if (configuration.isDetailLogging()) {
                    logDetails(connection);
                }
                if (connection.getResponseCode() < 200 ||
                        connection.getResponseCode() > 299) {
                    final Exception ex = new Exception(
                            "Invalid response code: " +
                                    connection.getResponseCode());
                    LOG.error("Can't download file: {}", ex);
                    exceptions.add(ex);
                    continue;
                }
                // Copy content.
                try (InputStream inputStream = connection.getInputStream()) {
                    FileUtils.copyInputStreamToFile(inputStream, work.target);
                } catch (IOException ex) {
                    LOG.error("Can't download file: {}", work.target, ex);
                    exceptions.add(ex);
                    continue;
                } finally {
                    final Date downloadEnded = new Date();
                    long downloadTime = downloadEnded.getTime() -
                            downloadStarted.getTime();
                    LOG.debug("Downloading of: {} takes: {} ms",
                            work.source, downloadTime);
                    progressReport.entryProcessed();
                    connection.disconnect();
                }
            }
            return null;
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(UriDownloader.class);

    private final ProgressReport progressReport;

    private final HttpGetFilesConfiguration configuration;

    private final ConcurrentLinkedQueue<Exception> exceptions =
            new ConcurrentLinkedQueue<>();

    private long total;

    public UriDownloader(ProgressReport progressReport,
            HttpGetFilesConfiguration configuration) {
        this.progressReport = progressReport;
        this.configuration = configuration;
    }

    /**
     * Download given files.
     *
     * @param toDownload
     */
    public void download(ConcurrentLinkedQueue<FileToDownload> toDownload,
            long size) {
        this.total = size;
        progressReport.start(size);
        //
        final ExecutorService executor = Executors.newFixedThreadPool(
                configuration.getThreads());
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < configuration.getThreads(); ++i) {
            executor.submit(new Worker(toDownload, counter));
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

    /**
     * Create connection for given reference.
     *
     * @param target
     * @return
     */
    private HttpURLConnection createConnection(URL target,
            Map<String, String> headers) throws IOException {
        // Create connection.
        final HttpURLConnection connection =
                (HttpURLConnection) target.openConnection();
        // Set headers.
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        if (configuration.isForceFollowRedirect()) {
            // Open connection and check for reconnect.
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                final String location = connection.getHeaderField("Location");
                if (location == null) {
                    throw new IOException("Invalid redirect from :"
                            + target.toString());
                } else {
                    // Create new based on the redirect.
                    connection.disconnect();
                    final HttpURLConnection newConnection =
                            (HttpURLConnection) target.openConnection();
                    connection.getRequestProperties().entrySet()
                            .forEach((entry) -> {
                                entry.getValue().forEach((value) -> {
                                    newConnection.addRequestProperty(
                                            entry.getKey(), value);
                                });
                            });
                    return newConnection;
                }
            } else {
                return connection;
            }
        }
        return connection;
    }

    /**
     * Log details about the connection.
     * 
     * @param connection
     */
    private static void logDetails(HttpURLConnection connection) {
        final InputStream errStream = connection.getErrorStream();
        if (errStream != null) {
            try {
                LOG.debug("Error stream: {}",
                        IOUtils.toString(errStream, "UTF-8"));
            } catch (Throwable ex) {
                // Ignore.
            }
        }
        try {
            LOG.debug(" response code: {}", connection.getResponseCode());
        } catch (IOException ex) {
            LOG.warn("Can't read status code.");
        }
        for (String header : connection.getHeaderFields().keySet()) {
            for (String value : connection.getHeaderFields().get(header)) {
                LOG.debug(" header: {} : {}", header, value);
            }
        }
    }


}
