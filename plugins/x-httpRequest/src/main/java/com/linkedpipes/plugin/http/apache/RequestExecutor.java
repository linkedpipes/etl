package com.linkedpipes.plugin.http.apache;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * We can add custom redirect handling using:
 * https://www.baeldung.com/httpclient-redirect-on-http-post
 */
public class RequestExecutor {

    @FunctionalInterface
    public interface ResponseConsumer {

        void apply(HttpResponse response) throws LpException;

    }

    private final RequestConfiguration configuration;

    private final ResponseConsumer consumer;

    public RequestExecutor(RequestConfiguration configuration, ResponseConsumer consumer) {
        this.configuration = configuration;
        this.consumer = consumer;
    }

    public void execute() throws LpException, IOException {
        try (CloseableHttpClient httpClient = createClient()) {
            var request = createHttpMethod(configuration.url);
            HttpResponse response = httpClient.execute(request);
            consumer.apply(response);
        }
    }

    private CloseableHttpClient createClient() {
        var requestConfig = RequestConfig.custom();
        if (configuration.timeout != null) {
            configureTimeOut(requestConfig, configuration.timeout);
        }
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig.build())
                .build();
    }

    private void configureTimeOut(RequestConfig.Builder builder, int timeout) {
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

    private HttpRequestBase createHttpMethod(String url) throws LpException {
        HttpRequestBase result;
        URI uri = createUri(url);
        if (configuration.content.isEmpty()) {
            result = createHttpWithoutContent(uri);
        } else if (configuration.contentAsBody) {
            result = createHttpWithBody(uri);
        } else {
            result = createHttpWithMultipart(uri);
        }
        setHeader(result);
        return result;
    }

    private URI createUri(String urlAsString) throws LpException {
        URL url;
        try {
            URL rawUrl = new URL(urlAsString);
            url = new URL(
                    rawUrl.getProtocol(),
                    IDN.toASCII(rawUrl.getHost()),
                    rawUrl.getPort(),
                    rawUrl.getFile());
        } catch (IOException ex) {
            throw new LpException("Can't create URL '{}'.",
                    configuration.url, ex);
        }
        if (configuration.encodeUrl) {
            try {
                url = new URL(url.toURI().toASCIIString());
            } catch (IOException | URISyntaxException ex) {
                throw new LpException("Can't convert to URI: {}", url, ex);
            }
        }
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new LpException("Can't create URL '{}' to URI..",
                    configuration.url, ex);
        }

    }

    /**
     * Create request without any content.
     */
    private HttpRequestBase createHttpWithoutContent(URI uri) {
        return new HttpMethod(configuration.method, uri);
    }

    /**
     * Create request with content as a body. Exactly one content
     * file must be given.
     */
    private HttpRequestBase createHttpWithBody(URI uri) throws LpException {
        var result = new HttpMethodWithContent(configuration.method, uri);
        addPostBody(result);
        return result;
    }

    private void addPostBody(HttpEntityEnclosingRequestBase request) throws LpException {
        // https://www.baeldung.com/httpclient-post-http-request
        if (configuration.content.size() != 1) {
            throw new LpException("Expected one content for post got '{}'.",
                    configuration.content.size());
        }
        var content = configuration.content.get(0);
        var entity = new FileEntity(content.file, ContentType.create(content.contentType));
        request.setEntity(entity);
    }

    /**
     * Create request with multipart content.
     */
    private HttpRequestBase createHttpWithMultipart(URI uri) {
        var result = new HttpMethodWithContent(configuration.method, uri);
        addMultipart(result);
        return result;
    }

    private void addMultipart(HttpEntityEnclosingRequestBase request) {
        // https://www.baeldung.com/httpclient-multipart-upload
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.RFC6532);

        for (RequestConfiguration.Content content : configuration.content) {
            if (content.file == null) {
                builder.addTextBody(
                        content.name,
                        content.value,
                        ContentType.TEXT_PLAIN);
            } else {
                builder.addBinaryBody(
                        content.name,
                        content.file,
                        ContentType.create(content.contentType),
                        content.fileName);
            }
        }
        HttpEntity entity = builder.build();
        request.setEntity(entity);
    }

    private void setHeader(HttpRequestBase request) {
        // https://www.baeldung.com/httpclient-custom-http-header
        for (var header : configuration.headers.entrySet()) {
            request.setHeader(header.getKey(), header.getValue());
        }
    }

}

