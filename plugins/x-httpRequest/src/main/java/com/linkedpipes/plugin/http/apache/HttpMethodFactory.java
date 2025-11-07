package com.linkedpipes.plugin.http.apache;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.IOException;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Given a request configuration prepare HTTP request that can be
 * executed.
 */
class HttpMethodFactory {

    public HttpRequestBase build(HttpRequest request) throws LpException {
        HttpRequestBase result;
        URI uri = createUri(request);
        if (request.content.isEmpty()) {
            result = createHttpWithoutContent(request, uri);
        } else if (request.contentAsBody) {
            result = createHttpWithBody(request, uri);
        } else {
            result = createHttpWithMultipart(request, uri);
        }
        setHeader(result, request.headers);
        return result;
    }

    private URI createUri(HttpRequest request) throws LpException {
        var urlAsString = request.url;
        URL url;
        try {
            URL rawUrl = new URL(urlAsString);
            url = new URL(
                    rawUrl.getProtocol(),
                    IDN.toASCII(rawUrl.getHost()),
                    rawUrl.getPort(),
                    rawUrl.getFile());
        } catch (IOException ex) {
            throw new LpException("Can't create URL '{}'.", request.url, ex);
        }
        if (request.encodeUrl) {
            try {
                url = new URL(url.toURI().toASCIIString());
            } catch (IOException | URISyntaxException ex) {
                throw new LpException("Can't convert '{}' to URL.", url, ex);
            }
        }
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new LpException("Can't create URL '{}'.", request.url, ex);
        }
    }

    /**
     * Create request without any content.
     */
    private HttpRequestBase createHttpWithoutContent(HttpRequest request, URI uri) {
        return new HttpMethod(request.method, uri);
    }

    /**
     * Create request with content as a body. Exactly one content
     * file must be given.
     */
    private HttpRequestBase createHttpWithBody(
            HttpRequest request, URI uri) throws LpException {
        var result = new HttpMethodWithContent(request.method, uri);
        if (request.content.size() != 1) {
            throw new LpException("Expected one content for post got '{}'.",
                    request.content.size());
        }
        addPostBody(result, request.content.getFirst());
        return result;
    }

    private void addPostBody(
            HttpEntityEnclosingRequestBase request,
            HttpRequest.Content content) {
        // https://www.baeldung.com/httpclient-post-http-request
        var entity = new FileEntity(content.file, asContentType(content.contentType));
        request.setEntity(entity);
    }

    private ContentType asContentType(String contentType) {
        if (contentType == null) {
            return ContentType.DEFAULT_BINARY;
        }
        return ContentType.create(contentType);
    }

    /**
     * Create request with multipart content.
     */
    private HttpRequestBase createHttpWithMultipart(
            HttpRequest request, URI uri) {
        var result = new HttpMethodWithContent(request.method, uri);
        addMultipart(result, request.content);
        return result;
    }

    private void addMultipart(
            HttpEntityEnclosingRequestBase request,
            Collection<HttpRequest.Content> contents) {
        // https://www.baeldung.com/httpclient-multipart-upload
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.RFC6532);

        for (HttpRequest.Content content : contents) {
            if (content.file == null) {
                builder.addTextBody(
                        content.name,
                        content.value,
                        ContentType.TEXT_PLAIN);
            } else {
                builder.addBinaryBody(
                        content.name,
                        content.file,
                        asContentType(content.contentType),
                        content.fileName);
            }
        }
        HttpEntity entity = builder.build();
        request.setEntity(entity);
    }

    private void setHeader(HttpRequestBase request, Map<String, String> headers) {
        // https://www.baeldung.com/httpclient-custom-http-header
        for (var header : headers.entrySet()) {
            request.setHeader(header.getKey(), header.getValue());
        }
    }

}
