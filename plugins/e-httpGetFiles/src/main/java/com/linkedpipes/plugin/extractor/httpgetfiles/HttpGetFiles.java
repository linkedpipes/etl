package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentLinkedQueue;

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
        // Prepare work.
        final ConcurrentLinkedQueue<UriDownloader.FileToDownload> workQueue =
                new ConcurrentLinkedQueue<>();
        for (HttpGetFilesConfiguration.Reference reference
                : configuration.getReferences()) {
            if (reference.getUri() == null
                    || reference.getUri().isEmpty()) {
                if (configuration.isSkipOnError()) {
                    continue;
                } else {
                    throw exceptionFactory.failure("Missing property: {}",
                            HttpGetFilesVocabulary.HAS_URI);
                }
            }
            if (reference.getFileName() == null
                    || reference.getFileName().isEmpty()) {
                if (configuration.isSkipOnError()) {
                    continue;
                } else {
                    throw exceptionFactory.failure("Missing property: {}",
                            HttpGetFilesVocabulary.HAS_NAME);
                }
            }
            final File target = output.createFile(
                    reference.getFileName()).toFile();
            final URL source;
            try {
                source = new URL(reference.getUri());
            } catch (MalformedURLException ex) {
                if (configuration.isSkipOnError()) {
                    LOG.warn("Invalid property: {} on {}");
                    continue;
                } else {
                    throw exceptionFactory.failure("Invalid property: {} on {}",
                            HttpGetFilesVocabulary.HAS_URI,
                            reference.getUri(), ex);
                }
            }
            // Prepare and and job.
            final UriDownloader.FileToDownload job =
                    new UriDownloader.FileToDownload(source, target);
            for (HttpGetFilesConfiguration.Header header
                    : configuration.getHeaders()) {
                job.setHeader(header.getKey(), header.getValue());
            }
            for (HttpGetFilesConfiguration.Header header
                    : reference.getHeaders()) {
                job.setHeader(header.getKey(), header.getValue());
            }
            workQueue.add(job);
        }
        // Execute.
        final UriDownloader downloader =
                new UriDownloader(progressReport, configuration);
        downloader.download(workQueue, configuration.getReferences().size());
        if (!downloader.getExceptions().isEmpty()) {
            int errorCounter = 0;
            for (Exception exception : downloader.getExceptions()) {
                ++errorCounter;
                LOG.error("Can't download.", exception);
            }
            LOG.info("Downloaded {}/{}", errorCounter,
                    configuration.getReferences());
            if (!configuration.isSkipOnError()) {
                throw exceptionFactory.failure("Can't download all entities.");
            }
        } else {
            LOG.info("Downloaded {}/{}", configuration.getReferences(),
                    configuration.getReferences());
        }

    }

    /**
     * Add trust to all certificates.
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

}
