package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import java.io.IOException;

public class TimeoutInjectHttpClient implements HttpClient {

    private HttpClient client;

    private TimeoutInjectHttpClient(HttpClient client) {
        this.client = client;
    }

    @Override
    public HttpParams getParams() {
        return client.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return client.getConnectionManager();
    }

    @Override
    public HttpResponse execute(HttpUriRequest request)
            throws IOException {
        injectTimeouts(request);
        return client.execute(request);
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context)
            throws IOException {
        injectTimeouts(request);
        return client.execute(request, context);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request)
            throws IOException {
        return client.execute(target, request);
    }

    @Override
    public HttpResponse execute(
            HttpHost target, HttpRequest request, HttpContext context)
            throws IOException {
        return client.execute(target, request);
    }

    @Override
    public <T> T execute(
            HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler)
            throws IOException {
        injectTimeouts(request);
        return client.execute(request, responseHandler);
    }

    @Override
    public <T> T execute(
            HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context)
            throws IOException {
        injectTimeouts(request);
        return client.execute(request, responseHandler, context);
    }

    @Override
    public <T> T execute(
            HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler)
            throws IOException {
        return client.execute(target, request, responseHandler);
    }

    @Override
    public <T> T execute(
            HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context)
            throws IOException {
        return client.execute(target, request, responseHandler, context);
    }

    private void injectTimeouts(HttpUriRequest request) {
        request.getParams().setIntParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, 5 * 1000);
    }

    public static void wrapForRepository(SPARQLRepository repository) {
        HttpClient originalClient =  repository.getHttpClient();
        HttpClient wrappedClient = new TimeoutInjectHttpClient(originalClient);
        repository.setHttpClient(wrappedClient);
    }

}
