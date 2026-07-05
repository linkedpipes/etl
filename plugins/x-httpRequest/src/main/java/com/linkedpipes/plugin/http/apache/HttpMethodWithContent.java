package com.linkedpipes.plugin.http.apache;

import java.net.URI;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

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
