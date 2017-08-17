package com.linkedpipes.plugin.extractor.httpgetfiles;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;

class Downloader {

    public static class Task {

        private final String getSourceUrl;

        private final File targetFile;

        private final Map<String, String> headers;

        private final Integer timeOut;

        public Task(String getSourceUrl, File targetFile,
                Map<String, String> headers, Integer timeOut) {
            this.getSourceUrl = getSourceUrl;
            this.targetFile = targetFile;
            this.headers = headers;
            this.timeOut = timeOut;
        }

        public String getSourceUrl() {
            return getSourceUrl;
        }

        public File getTargetFile() {
            return targetFile;
        }

        public Map<String, String> getHeader() {
            return headers;
        }

        public Integer getTimeOut() {
            return timeOut;
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);

    private boolean followRedirect = false;

    private final Task toDownload;

    private boolean logDetail = false;

    public Downloader(boolean followRedirect,
            Task toDownload, boolean logDetail) {
        this.followRedirect = followRedirect;
        this.toDownload = toDownload;
        this.logDetail = logDetail;
    }

    public void download() throws IOException {
        LOG.info("Downloading: {} -> {}", toDownload.getSourceUrl(),
                toDownload.getTargetFile().toString());
        //
        URL url = createUrl(toDownload.getSourceUrl());
        HttpURLConnection connection = null;
        Date startTime = new Date();
        try {
            connection = createConnectionFollowRedirect(url);
            checkResponseCode(connection);
            saveContentToFile(connection, toDownload.getTargetFile());
        } catch (RuntimeException ex) {
            throw new IOException("Can't download file.", ex);
        } finally {
            Date endTime = new Date();
            long downloadTime = endTime.getTime() - startTime.getTime();
            LOG.debug("Processing of: {} takes: {} ms",
                    toDownload.getSourceUrl(), downloadTime);
            //
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private URL createUrl(String stringAsUrl) throws IOException {
        return new URL(stringAsUrl);
    }

    private HttpURLConnection createConnectionFollowRedirect(URL target)
            throws IOException {
        HttpURLConnection connection = createConnection(target);
        if (followRedirect) {
            return updateConnectionIfRedirected(connection);
        } else {
            return connection;
        }
    }

    private HttpURLConnection createConnection(URL target) throws IOException {
        HttpURLConnection connection =
                (HttpURLConnection) target.openConnection();
        setHeaders(connection);
        setTimeOut(connection);
        if (logDetail) {
            logConnectionDetails(connection);
        }
        return connection;
    }

    private void setHeaders(HttpURLConnection connection) {
        Map<String, String> headers = toDownload.getHeader();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private void setTimeOut(HttpURLConnection connection) {
        Integer timeOut = toDownload.getTimeOut();
        if (timeOut != null) {
            connection.setConnectTimeout(timeOut);
            connection.setReadTimeout(timeOut);
        }
    }

    private void logConnectionDetails(HttpURLConnection connection) {
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

    private HttpURLConnection updateConnectionIfRedirected(
            HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (isResponseRedirect(responseCode)) {
            connection.disconnect();
            String location = connection.getHeaderField("Location");
            LOG.debug("Resolved redirect to: {}", location);
            return createConnection(createUrl(location));
        } else {
            return connection;
        }
    }

    private boolean isResponseRedirect(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER;
    }

    private void checkResponseCode(HttpURLConnection connection)
            throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode > 299) {
            IOException ex = new IOException("Invalid response code: " +
                    responseCode + " message: " +
                    connection.getResponseMessage());
            LOG.error("Can't download file: {}", toDownload.getSourceUrl(), ex);
            throw ex;
        }
    }

    private void saveContentToFile(HttpURLConnection connection, File file)
            throws IOException {
        try (InputStream inputStream = connection.getInputStream()) {
            FileUtils.copyInputStreamToFile(inputStream, file);
        }
    }

    public void setFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
    }

    public boolean isLogDetail() {
        return logDetail;
    }

}
