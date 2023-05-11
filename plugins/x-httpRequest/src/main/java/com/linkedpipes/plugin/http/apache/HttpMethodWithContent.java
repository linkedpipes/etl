package com.linkedpipes.plugin.http.apache;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

class HttpMethodWithContent extends HttpEntityEnclosingRequestBase {

    final String method;

    public HttpMethodWithContent(String method, URI uri) {
        this.setURI(uri);
        this.method = method;
    }

    @Override
    public String getMethod() {
        return method;
    }

}
