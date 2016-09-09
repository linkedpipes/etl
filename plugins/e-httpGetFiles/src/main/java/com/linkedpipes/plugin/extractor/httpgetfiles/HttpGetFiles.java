package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Škoda Petr
 */
public final class HttpGetFiles implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(HttpGetFiles.class);

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public HttpGetFilesConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        // TODO Do not use this, but be selective about certs we trust.
        try {
            LOG.warn("'Trust all certs' policy used -> security risk!");
            setTrustAllCerts();
        } catch (Exception ex) {
            throw exceptionFactory.failure(
                    "Can't set trust all certificates.", ex);
        }
        progressReport.start(configuration.getReferences().size());
        for (HttpGetFilesConfiguration.Reference reference
                : configuration.getReferences()) {
            try {
                download(reference);
            } catch (Throwable t) {
                if (configuration.isSkipOnError()) {
                    LOG.warn("Skipping file: {} -> {}", reference.getFileName(),
                            reference.getUri(), t);
                } else {
                    throw t;
                }
            }
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    /**
     * Download and store referenced resource.
     *
     * @param reference
     * @throws LpException
     */
    private void download(HttpGetFilesConfiguration.Reference reference)
            throws LpException {
        if (reference.getUri() == null
                || reference.getUri().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    HttpGetFilesVocabulary.HAS_URI);
        }
        if (reference.getFileName() == null
                || reference.getFileName().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    HttpGetFilesVocabulary.HAS_NAME);
        }
        LOG.info("Downloading: {} -> {}", reference.getUri(),
                reference.getFileName());
        // Prepare target destination.
        final File destination = output.createFile(
                reference.getFileName()).toFile();
        // Download file.
        HttpURLConnection connection;
        try {
            connection = createConnection(reference);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't open connection.", ex);
        }
        if (configuration.isForceFollowRedirect()) {
            // Check for redirect. We can have multiple redirects
            // so follow until there is no one.
            HttpURLConnection oldConnection;
            try {
                do {
                    oldConnection = connection;
                    connection = followRedirect(oldConnection);
                } while (connection != oldConnection);
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't resolve redirect.", ex);
            }
        }
        // Copy content.
        try (InputStream inputStream = connection.getInputStream()) {
            FileUtils.copyInputStreamToFile(inputStream, destination);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't copy file.", ex);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Add trust to all certificates.
     *
     * @throws Exception
     */
    private static void setTrustAllCerts() throws Exception {
        final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[]
                        getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) {
                }
            }
        };
        // Install the all-trusting trust manager.
        try {
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                    (String urlHostName, SSLSession session) -> true);
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            throw ex;
        }
    }

    /**
     * Open connection and check for redirect. If there is redirect then
     * close given connection and return connection to the new location.
     *
     * @param connection
     * @return
     * @throws IOException
     * @throws LpException
     */
    private HttpURLConnection followRedirect(HttpURLConnection connection)
            throws IOException, LpException {
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER ) {
            final String location = connection.getHeaderField("Location");
            if (location == null) {
                throw exceptionFactory.failure("Missing Location for redirect.");
            } else {
                // Update based on the redirect.
                connection.disconnect();
                LOG.debug("Follow redirect: {}", location);
                return createConnection(new URL(location), connection);
            }
        } else {
            return connection;
        }
    }

    /**
     * Create connection for given reference.
     *
     * @param reference
     * @return
     * @throws IOException
     * @throws LpException
     */
    private HttpURLConnection createConnection(
            HttpGetFilesConfiguration.Reference reference)
            throws IOException, LpException {
        // Prepare target location.
        final URL target;
        try {
            target = new URL(reference.getUri());
        } catch (MalformedURLException ex) {
            throw exceptionFactory.failure("Invalid property: {} on {}",
                    HttpGetFilesVocabulary.HAS_URI,
                    reference.getUri(), ex);
        }
        // Prepare headers.
        final Map<String, String> headers = new HashMap<>();
        for (HttpGetFilesConfiguration.Header header
                : configuration.getHeaders()) {
            headers.put(header.getKey(), header.getValue());
        }
        for (HttpGetFilesConfiguration.Header header
                : reference.getHeaders()) {
            headers.put(header.getKey(), header.getValue());
        }
        // Create connection.
        final HttpURLConnection connection =
                (HttpURLConnection) target.openConnection();
        // Set headers.
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        return connection;
    }

    /**
     * Create and return connection to given destination. Copy used headers.
     *
     * @param target
     * @param originalConnection
     * @return
     * @throws IOException
     */
    private HttpURLConnection createConnection(URL target,
            HttpURLConnection originalConnection) throws IOException {
        final HttpURLConnection connection =
                (HttpURLConnection) target.openConnection();
        // Copy properties.
        originalConnection.getRequestProperties().entrySet().forEach((entry) -> {
            entry.getValue().forEach((value) -> {
                connection.addRequestProperty(entry.getKey(), value);
            });
        });
        return connection;
    }

}
