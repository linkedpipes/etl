package com.linkedpipes.plugin.http.apache;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.IOException;

/**
 * We can add custom redirect handling using:
 */
public class RequestExecutor {

    /**
     * Interface for a consumer.
     */
    @FunctionalInterface
    public interface ResponseConsumer {

        void apply(HttpResponse response) throws LpException;

    }

    private final HttpRequest request;

    private final ResponseConsumer consumer;

    public RequestExecutor(HttpRequest request, ResponseConsumer consumer) {
        this.request = request;
        this.consumer = consumer;
    }

    /**
     * Execute the connection and call {@link #consumer} when the final response is ready.
     */
    public void execute() throws LpException, IOException {
        var request = (new HttpMethodFactory()).build((this.request));
        try (CloseableHttpClient httpClient = createClient()) {
            HttpResponse response = httpClient.execute(request);
            consumer.apply(response);
        }
    }

    private CloseableHttpClient createClient() {
        var clientBuilder = HttpClientBuilder.create();
        if (request.followRedirect) {
            // https://www.baeldung.com/httpclient-redirect-on-http-post
            // DefaultRedirectStrategy - works only for "GET", "HEAD"
            // LaxRedirectStrategy - works only for "GET", "POST", "HEAD", "DELETE"
            clientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        }

        var requestBuilder = RequestConfig.custom();
        configureTimeOut(requestBuilder, request.timeout);
        clientBuilder.setDefaultRequestConfig(requestBuilder.build());

        return clientBuilder.build();
    }

    private void configureTimeOut(RequestConfig.Builder builder, Integer timeout) {
        if (timeout == null) {
            // Nothing to set here.
            return;
        }
        // We can also set hard-time out for whole request.
        // https://www.baeldung.com/httpclient-timeout
        // The time to establish the connection with the remote host.
        builder.setConnectTimeout(timeout)
                // The time waiting for data – after establishing the
                // connection; maximum time of inactivity between two
                // data packets.
                .setConnectionRequestTimeout(timeout)
                // The time to wait for a connection from the connection
                // manager/pool.
                .setSocketTimeout(timeout);
    }

}
