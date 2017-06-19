package com.linkedpipes.plugin.exec.graphstorepurger;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.rdf.utils.RdfUtils;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfSource;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class GraphStorePurger implements Component, SequentialExecution {

    private static final Logger LOG =
            LoggerFactory.getLogger(GraphStorePurger.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "Tasks")
    public SingleGraphDataUnit taskRdf;

    @Component.Configuration
    public GraphStorePurgerConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    private List<GraphsToPurge> graphsToPurge;

    private CloseableHttpClient httpClient;

    @Override
    public void execute() throws LpException {
        loadTasks();
        createHttpClient();
        try {
            purgeEndpoint();
        } finally {
            closeHttpClient();
        }
    }

    private void loadTasks() throws LpException {
        RdfSource source = Rdf4jSource.createWrap(taskRdf.getRepository());
        try {
            graphsToPurge = RdfUtils.loadList(source,
                    taskRdf.getReadGraph().stringValue(),
                    RdfToPojo.descriptorFactory(),
                    GraphsToPurge.class);
        } catch (RdfUtilsException ex) {
            exceptionFactory.failure("Can't load input tasks.", ex);
        }
    }

    private void purgeEndpoint() throws LpException {
        HttpClientContext context = HttpClientContext.create();
        if (configuration.isUseAuthentication()) {
            requestForPreemptiveAuthentication(context);
        }
        for (GraphsToPurge task : graphsToPurge) {
            for (String graph : task.getGraphs()) {
                deleteGraph(context, graph);
            }
        }
    }

    private void closeHttpClient() {
        try {
            httpClient.close();
        } catch (IOException ex) {
            LOG.error("Can't close HTTP client.", ex);
        }
    }

    private void createHttpClient() throws LpException {
        if (configuration.isUseAuthentication()) {
            final RequestConfig requestConfig = RequestConfig.custom()
                    .setAuthenticationEnabled(true).build();
            this.httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultCredentialsProvider(createCredentialsProvider())
                    .build();
        } else {
            this.httpClient = HttpClients.custom().build();
        }
    }

    private CredentialsProvider createCredentialsProvider() {
        CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(
                        configuration.getUsername(),
                        configuration.getPassword()));
        return credentialsProvider;
    }

    /**
     * Do an empty request just to get the validation into a cache.
     * This is requires as for example Virtuoso will refuse the first
     * request and ask for authorization. However as the first request
     * can be too big - it would looks like a failure to us
     * (as Virtuoso just close the connection before reading all the data).
     */
    private void requestForPreemptiveAuthentication(
            HttpClientContext context) {
        final HttpEntityEnclosingRequestBase emptyRequest
                = new HttpPut(configuration.getEndpoint());
        try (CloseableHttpResponse response
                     = this.httpClient.execute(emptyRequest, context)) {
        } catch (Exception ex) {
            LOG.info("Exception during first empty request.", ex);
        }
    }

    private void deleteGraph(
            HttpClientContext context,
            String graph) throws LpException {
        String url = configuration.getEndpoint() + "?graph-uri=";
        try {
            url += URLEncoder.encode(graph, "UTF-8");
        } catch (IOException ex) {
            throw exceptionFactory.failure("Unknown encoding.", ex);
        }
        HttpDelete httpMethod = new HttpDelete(url);
        try {
            executeHttpRequest(context, httpMethod);
        } catch (LpException ex) {
            throw exceptionFactory.failure("Delete request failed on: {}",
                    url, ex);
        }
    }

    private void executeHttpRequest(
            HttpClientContext context,
            HttpDelete httpMethod) throws LpException {
        try (CloseableHttpResponse response
                     = this.httpClient.execute(httpMethod, context)) {
            checkResponse(response);
        } catch (IOException | ParseException ex) {
            throw exceptionFactory.failure("Can't execute request.", ex);
        }
    }

    private void checkResponse(HttpResponse response) throws LpException {
        try {
            LOG.debug("Response:\n {} ",
                    EntityUtils.toString(response.getEntity()));
        } catch (IOException ex) {
            LOG.error("Can't read response.", ex);
        }
        LOG.info("Response code: {} phrase: {}",
                response.getStatusLine().getStatusCode(),
                response.getStatusLine().getReasonPhrase());
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 404) {
            // This mean that there is no such graph, so we can consider
            // it to be deleted.
            return;
        }
        if (statusCode >= 400) {
            throw exceptionFactory.failure(
                    "Invalid response: {}",
                    response.getStatusLine().getReasonPhrase());
        }
    }

}
