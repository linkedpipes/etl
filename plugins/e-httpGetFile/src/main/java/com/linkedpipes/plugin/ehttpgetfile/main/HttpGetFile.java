package com.linkedpipes.plugin.ehttpgetfile.main;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.etl.plugin.api.v2.ComponentV2;
import com.linkedpipes.plugin.ehttpgetfile.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@ComponentV2.IRI(HttpGetFileVocabulary.IRI)
public final class HttpGetFile implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(HttpGetFile.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public HttpGetFileConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        checkConfiguration();
        setTrustAllCerts();
        progressReport.start(1);
        File outputFile = output.createFile(configuration.getFileName());
        Map<String, String> header = createHeader();
        Downloader.Task fileToDownload = new Downloader.Task(
                configuration.getUri(), outputFile, header, null);
        Downloader downloader = new Downloader(
                fileToDownload, configuration.asDownloaderConfiguration());
        try {
            downloader.download();
        } catch (Exception ex) {
            throw new LpException("Can't download file.", ex);
        }
        progressReport.entryProcessed();
        progressReport.done();
    }

    private void checkConfiguration() throws LpException {
        if (isNullOrEmpty(configuration.getUri())) {
            throw new LpException("Missing property: {}",
                    HttpGetFileVocabulary.HAS_URI);
        }
        if (isNullOrEmpty(configuration.getFileName())) {
            throw new LpException("Missing property: {}",
                    HttpGetFileVocabulary.HAS_NAME);
        }
    }

    private void setTrustAllCerts() throws LpException {
        LOG.warn("'Trust all certs' policy used -> security risk!");
        TrustManager[] trustAllCerts = new TrustManager[]{
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
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                    (String urlHostName, SSLSession session) -> true);
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            throw new LpException("Can't set trust all certificates.", ex);
        }
    }

    private Map<String, String> createHeader() {
        Map<String, String> header = new HashMap<>();
        if (!isNullOrEmpty(configuration.getUserAgent())) {
            header.put("user-agent", configuration.getUserAgent());
        }
        return header;
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

}
