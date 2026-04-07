package com.linkedpipes.plugin.ehttpgetfile;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Downloader {

    /**
     * Called on final connection before checking status code.
     */
    @FunctionalInterface
    public interface ReportConnection {

        void accept(HttpURLConnection connection) throws LpException;

    }

    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);

    /**
     * @param request       Configuration to use for this and all derived requests.
     * @param urlToDownload URL to download from.
     * @param downloadPath  Path where to download to.
     * @throws IOException
     */
    public void download(
            DownloaderRequest request,
            String urlToDownload,
            File downloadPath,
            ReportConnection reportConnection
    ) throws IOException, LpException {
        LOG.info("Downloading '{}' -> '{}'.", urlToDownload, downloadPath);
        HttpURLConnection connection = null;
        try {
            URL url = createUrl(request, urlToDownload);
            connection = createConnection(request, url);
            reportConnection.accept(connection);
            checkResponse(request, connection);
            saveContentToFile(connection, downloadPath);
        } catch (RuntimeException ex) {
            throw new IOException("Can't download file.", ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private URL createUrl(DownloaderRequest request, String urlAsString)
            throws IOException {
        URL url;
        try {
            // Parse so we have access to parts.
            var urlParts = new URL(urlAsString);
            // Encode the host to support IDN.
            url = new URL(
                    urlParts.getProtocol(),
                    IDN.toASCII(urlParts.getHost()),
                    urlParts.getPort(),
                    urlParts.getFile());
        } catch (IOException ex) {
            throw new IOException("Can't create URL.", ex);
        }
        if (request.encodeUrl()) {
            try {
                url = new URL(url.toURI().toASCIIString());
            } catch (IOException | URISyntaxException ex) {
                throw new IOException("Can't convert to URI: " + url, ex);
            }
        }
        return url;
    }

    /**
     * Open connection to given URL, does not use the URL from the task.
     * This is so we can use it in redirect handling.
     */
    private HttpURLConnection createConnection(
            DownloaderRequest request, URL url
    ) throws IOException {
        var connection = (HttpURLConnection) url.openConnection();
        setHeaders(request, connection);
        setTimeOut(request, connection);
        if (request.logDetail()) {
            logConnectionDetails(connection);
        }
        if (request.manualRedirect()) {
            connection.setInstanceFollowRedirects(false);
            return resolveRedirects(request, connection);
        } else {
            connection.setInstanceFollowRedirects(true);
            return connection;
        }
    }

    private void setHeaders(
            DownloaderRequest request, HttpURLConnection connection
    ) {
        Map<String, String> headers = request.requestHeaders();
        // Fixed headers, #697.
        connection.setRequestProperty("accept-encoding", "gzip");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private void setTimeOut(
            DownloaderRequest request, HttpURLConnection connection
    ) {
        Integer timeOut = request.timeout();
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
                        IOUtils.toString(errStream, StandardCharsets.UTF_8));
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

    private HttpURLConnection resolveRedirects(
            DownloaderRequest request,
            HttpURLConnection connection) throws IOException {
        while (isResponseRedirect(connection)) {
            String location = connection.getHeaderField("Location");
            if (request.useUtf8ForRedirect()) {
                location = new String(
                        location.getBytes(StandardCharsets.ISO_8859_1),
                        StandardCharsets.UTF_8);
            }
            connection.disconnect();
            LOG.debug("Resolved redirect to: {}", location);
            var nextUrl = createUrl(request, location);
            connection = createConnection(request, nextUrl);
        }
        return connection;
    }

    private boolean isResponseRedirect(HttpURLConnection connection)
            throws IOException {
        int responseCode = connection.getResponseCode();
        // Redirect responses have status codes that start with 3,
        // and a Location header holding the URL to redirect to.
        return 299 < responseCode && responseCode < 400
                && connection.getHeaderField("Location") != null;
    }

    private void checkResponse(
            DownloaderRequest request, HttpURLConnection connection
    ) throws IOException {

        if (request.logDetail()) {
            for (Map.Entry<String, List<String>> entry :
                    connection.getHeaderFields().entrySet()) {
                LOG.info("Header: {}", entry.getKey());
                for (String value : entry.getValue()) {
                    LOG.info("  {}", value);
                }
            }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode > 299) {
            // Write response content.
            StringWriter writer = new StringWriter();
            try (InputStream err = connection.getErrorStream()) {
                if (err != null) {
                    IOUtils.copy(err, writer, "UTF-8");
                }
            }
            LOG.info("Error: {}", writer);
            // Throw an exception.
            throw new IOException(
                    responseCode + " : " + connection.getResponseMessage());
        }
    }

    private void saveContentToFile(HttpURLConnection connection, File file)
            throws IOException {
        InputStream inputStream;
        if (isGzip(connection)) {
            inputStream = new GZIPInputStream(connection.getInputStream());
        } else {
            inputStream = connection.getInputStream();
        }
        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
        } finally {
            inputStream.close();
        }
    }

    private boolean isGzip(HttpURLConnection connection) {
        Map<String, List<String>> headers = getNormalizedHeader(connection);
        if (!headers.containsKey("content-encoding")) {
            return false;
        }
        for (String value : headers.get("content-encoding")) {
            if ("gzip".equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, List<String>> getNormalizedHeader(
            HttpURLConnection connection) {
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry :
                connection.getHeaderFields().entrySet()) {
            if (entry.getKey() == null) {
                result.put(null, entry.getValue());
            } else {
                result.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }
        return result;
    }

}
