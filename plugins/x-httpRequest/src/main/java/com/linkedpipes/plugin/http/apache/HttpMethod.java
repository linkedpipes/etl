package com.linkedpipes.plugin.http.apache;

import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

class HttpMethod extends HttpRequestBase {

    final String method;

    public HttpMethod(String method, URI uri) {
        this.setURI(uri);
        this.method = method;
    }

    @Override
    public String getMethod() {
        return method;
    }

}
