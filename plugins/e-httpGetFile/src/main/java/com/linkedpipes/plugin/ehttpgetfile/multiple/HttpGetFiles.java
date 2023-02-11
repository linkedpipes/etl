package com.linkedpipes.plugin.ehttpgetfile.multiple;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecution;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.plugin.api.v2.ComponentV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@ComponentV2.IRI(HttpGetFilesVocabulary.IRI)
public final class HttpGetFiles extends TaskExecution<DownloadTask> {

    private static final Logger LOG =
            LoggerFactory.getLogger(HttpGetFiles.class);

    @ContainsConfiguration
    @InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @OutputPort(iri = "FilesOutput")
    public WritableFilesDataUnit output;

    @InputPort(iri = "ReportRdf")
    public WritableSingleGraphDataUnit reportRdf;

    @Configuration
    public HttpGetFilesConfiguration configuration;

    private StatementsConsumer statementsConsumer;

    private ReportWriter reportWriter;

    @Override
    protected void onInitialize(Context context) throws LpException {
        super.onInitialize(context);
        statementsConsumer = new StatementsConsumer(reportRdf);
        reportWriter = ReportWriter.create(reportRdf.getWriter());
    }

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        TaskExecutionConfiguration result = new TaskExecutionConfiguration();
        result.numberOfThreads = configuration.getThreads();
        result.numberOfThreadsPerGroup =
                configuration.getThreadsPerGroup();
        result.skipFailedTasks = configuration.isSkipOnError();
        result.numberOfRetries = configuration.getRetryCount();
        result.waitAfterFailedTaskMs = configuration.getRetryWaitTimeMs();
        result.waitAfterTaskMs  =configuration.getWaitTimeMs();
        return result;
    }

    @Override
    protected List<DownloadTask> loadTasks() throws LpException {
        RdfSource source = configurationRdf.asRdfSource();
        List<String> resources =
                source.getByType(HttpGetFilesVocabulary.REFERENCE);
        List<DownloadTask> result = new ArrayList<>(resources.size());
        for (String resource : resources) {
            DownloadTask task = new DownloadTask();
            RdfToPojoLoader.loadByReflection(source, resource, task);
            result.add(task);
        }
        return result;
    }

    @Override
    protected ReportWriter createReportWriter() {
        return reportWriter;
    }

    @Override
    protected TaskConsumer<DownloadTask> createConsumer() {
        return new DownloadTaskExecutor(
                configuration, output, statementsConsumer, reportWriter);
    }

    @Override
    protected void onExecutionWillBegin(List<DownloadTask> tasks)
            throws LpException {
        super.onExecutionWillBegin(tasks);
        setTrustAllCerts();
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
            throw new LpException(
                    "Can't set trust all certificates.", ex);
        }
    }

}
