package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class HttpGetFiles implements Component, SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(HttpGetFiles.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public HttpGetFilesConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        // TODO Do not use this, but be selective about certs we trust.
        try {
            setTrustAllCerts();
        } catch (Exception ex) {
            throw exceptionFactory.failure(
                    "Can't set trust all certificates.", ex);
        }
        List<FileToDownload> filesToDownload = getFilesToDownload();
        // Prepare work.
        ConcurrentLinkedQueue<UriDownloader.FileToDownload> workQueue =
                new ConcurrentLinkedQueue<>();
        for (FileToDownload reference : filesToDownload) {
            UriDownloader.FileToDownload job = createDownloadJob(reference);
            if (job == null) {
                continue;
            }
            workQueue.add(job);
        }
        // Execute.
        UriDownloader downloader = new UriDownloader(progressReport, configuration);
        downloader.download(workQueue, filesToDownload.size());
        if (!downloader.getExceptions().isEmpty()) {
            LOG.info("Downloaded {}/{}", downloader.getExceptions().size(),
                    filesToDownload.size());
            if (!configuration.isSkipOnError()) {
                throw exceptionFactory.failure("Can't download all entities.");
            }
        } else {
            LOG.info("Downloaded {}/{}", filesToDownload.size(),
                    filesToDownload.size());
        }
    }

    private List<FileToDownload> getFilesToDownload() throws LpException {
        RdfSource source = Rdf4jSource.wrapRepository(
                configurationRdf.getRepository());
        try {
            return RdfUtils.loadList(source,
                    configurationRdf.getReadGraph().stringValue(),
                    RdfToPojo.descriptorFactory(),
                    FileToDownload.class);
        } catch (RdfUtilsException ex) {
            throw exceptionFactory.failure("Can't load tasks.", ex);
        }
    }

    private void setTrustAllCerts() throws Exception {
        LOG.warn("'Trust all certs' policy used -> security risk!");
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

    private UriDownloader.FileToDownload createDownloadJob(
            FileToDownload reference) throws LpException {
        String uri = getUri(reference);
        String fileName = getFileName(reference);
        if (uri == null || fileName == null) {
            if (configuration.isSkipOnError()) {
                return null;
            } else {
                throw exceptionFactory.failure("Invalid reference: {}",
                        reference.getUri());
            }
        }
        URL source = createUrl(uri);
        if (source == null) {
            return null;
        }
        File target = output.createFile(reference.getFileName());
        UriDownloader.FileToDownload job = new UriDownloader.FileToDownload(
                source, target);
        job.setTimeOut(getTimeOut(reference));
        setHeaders(job, reference);
        return job;

    }

    private String getUri(FileToDownload reference) throws LpException {
        String uri = reference.getUri();
        if (uri == null || uri.isEmpty()) {
            return null;
        }
        return uri;
    }

    private String getFileName(FileToDownload reference)
            throws LpException {
        String fileName = reference.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        return fileName;
    }

    private URL createUrl(String urlAsString) throws LpException {
        try {
            return new URL(urlAsString);
        } catch (MalformedURLException ex) {
            if (configuration.isSkipOnError()) {
                LOG.warn("Invalid property: {} on {}");
                return null;
            } else {
                throw exceptionFactory.failure("Invalid property: {} on {}",
                        HttpGetFilesVocabulary.HAS_URI,
                        urlAsString, ex);
            }
        }
    }

    private Integer getTimeOut(FileToDownload reference) {
        Integer timeOut = reference.getTimeOut();
        if (timeOut == null) {
            return configuration.getTimeout();
        } else {
            return timeOut;
        }
    }

    private void setHeaders(UriDownloader.FileToDownload job,
            FileToDownload reference) {
        for (RequestHeader header : configuration.getHeaders()) {
            job.setHeader(header.getKey(), header.getValue());
        }
        for (RequestHeader header : reference.getHeaders()) {
            job.setHeader(header.getKey(), header.getValue());
        }
    }

}
