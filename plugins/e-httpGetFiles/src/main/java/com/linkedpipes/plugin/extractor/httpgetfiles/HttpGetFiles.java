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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        setTrustAllCerts();
        List<FileToDownload> filesToDownload = getFilesToDownload();
        //
        ConcurrentLinkedQueue<Downloader.Task> workQueue =
                new ConcurrentLinkedQueue<>();
        for (FileToDownload reference : filesToDownload) {
            Downloader.Task task = createDownloadTask(reference);
            if (task == null) {
                continue;
            }
            workQueue.add(task);
        }
        // Execute.
        UriDownloader downloader =
                new UriDownloader(progressReport, configuration);
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

    private void setTrustAllCerts() throws LpException {
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
            throw exceptionFactory.failure(
                    "Can't set trust all certificates.", ex);
        }
    }

    private Downloader.Task createDownloadTask(FileToDownload reference)
            throws LpException {
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
        File targetFile = output.createFile(reference.getFileName());
        Downloader.Task job = new Downloader.Task(uri,
                targetFile, getHeader(reference), getTimeOut(reference));
        return job;

    }

    private String getUri(FileToDownload reference) throws LpException {
        String uri = reference.getUri();
        if (uri == null || uri.isEmpty()) {
            return null;
        }
        return uri;
    }

    private String getFileName(FileToDownload reference) throws LpException {
        String fileName = reference.getFileName();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        return fileName;
    }

    private Map<String, String> getHeader(FileToDownload reference) {
        Map<String, String> headers = new HashMap<>();
        for (RequestHeader header : configuration.getHeaders()) {
            headers.put(header.getKey(), header.getValue());
        }
        for (RequestHeader header : reference.getHeaders()) {
            headers.put(header.getKey(), header.getValue());
        }
        return headers;
    }

    private Integer getTimeOut(FileToDownload reference) {
        Integer timeOut = reference.getTimeOut();
        if (timeOut == null) {
            return configuration.getTimeout();
        } else {
            return timeOut;
        }
    }

}
