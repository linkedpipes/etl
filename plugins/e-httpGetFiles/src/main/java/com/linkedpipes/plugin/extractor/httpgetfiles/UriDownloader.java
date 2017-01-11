package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.component.api.service.ProgressReport;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

class UriDownloader {

    /**
     * Represent a file to download.
     */
    public static class FileToDownload {

        private final URL source;

        private final File target;

        private Map<String, String> headers = new HashMap<>();

        public FileToDownload(URL source, File targer) {
            this.source = source;
            this.target = targer;
        }

        public void setHeader(String key, String value) {
            headers.put(key, value);
        }
    }

    private class Worker implements Callable<Object> {

        private final ConcurrentLinkedQueue<FileToDownload> workQueue;

        public Worker(
                ConcurrentLinkedQueue<FileToDownload> workQueue) {
            this.workQueue = workQueue;
        }

        @Override
        public Object call() throws Exception {
            FileToDownload work;
            while ((work = workQueue.poll()) != null) {
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
                    exceptions.add(ex);
                    progressReport.entryProcessed();
                    continue;
                }
                // Copy content.
                try (InputStream inputStream = connection.getInputStream()) {
                    FileUtils.copyInputStreamToFile(inputStream, work.target);
                } catch (IOException ex) {
                    exceptions.add(ex);
                    continue;
                } finally {
                    progressReport.entryProcessed();
                    connection.disconnect();
                }
            }
            return null;
        }
    }

    private final ProgressReport progressReport;

    private final HttpGetFilesConfiguration configuration;

    private final ConcurrentLinkedQueue<Exception> exceptions =
            new ConcurrentLinkedQueue<>();

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
    public void download(ConcurrentLinkedQueue<FileToDownload> toDownload) {
        final ExecutorService executor = Executors.newFixedThreadPool(
                configuration.getThreads());
        progressReport.start(toDownload.size());
        for (int i = 0; i < configuration.getThreads(); ++i) {
            executor.submit(new Worker(toDownload));
        }
        progressReport.done();
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

    private HttpURLConnection copyConnectionHeader(URL target,
            HttpURLConnection originalConnection) throws IOException {
        final HttpURLConnection connection =
                (HttpURLConnection) target.openConnection();
        // Copy properties.
        originalConnection.getRequestProperties().entrySet()
                .forEach((entry) -> {
                    entry.getValue().forEach((value) -> {
                        connection.addRequestProperty(entry.getKey(), value);
                    });
                });
        return connection;
    }

}
