package com.linkedpipes.plugin.ehttpgetfile.multiple;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskConsumer;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecution;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.component.task.TaskSource;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
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

    @Inject
    public ProgressReport progressReport;

    private List<DownloadTask> tasks;

    private StatementsConsumer statementsConsumer;

    private ReportWriter reportWriter;

    @Override
    protected TaskSource<DownloadTask> createTaskSource() throws LpException {
        loadTasks();
        TaskSource<DownloadTask> source = TaskSource.groupTaskSource(
                this.tasks, configuration.getThreadsPerGroup());
        source.setSkipOnError(configuration.isSkipOnError());
        return source;
    }

    private void loadTasks() throws LpException {
        RdfSource source = configurationRdf.asRdfSource();
        List<String> resources =
                source.getByType(HttpGetFilesVocabulary.REFERENCE);
        tasks = new ArrayList<>(resources.size());
        for (String resource : resources) {
            DownloadTask task = new DownloadTask();
            RdfToPojoLoader.loadByReflection(source, resource, task);
            tasks.add(task);
        }
    }

    @Override
    protected TaskExecutionConfiguration getExecutionConfiguration() {
        return this.configuration;
    }

    @Override
    protected TaskConsumer<DownloadTask> createConsumer() {
        return new DownloadTaskExecutor(
                configuration, progressReport, output,
                statementsConsumer, reportWriter);
    }

    @Override
    protected ReportWriter createReportWriter() {
        if (reportWriter != null) {
            return reportWriter;
        }
        reportWriter = ReportWriter.create(reportRdf.getWriter());
        return reportWriter;
    }

    @Override
    protected void initialization() throws LpException {
        super.initialization();
        statementsConsumer = new StatementsConsumer(reportRdf);
        reportWriter = createReportWriter();
    }

    @Override
    protected void beforeExecution() throws LpException {
        super.beforeExecution();
        setTrustAllCerts();
        progressReport.start(tasks);
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

    @Override
    protected void afterExecution() throws LpException {
        super.afterExecution();
        this.progressReport.done();
    }

}
