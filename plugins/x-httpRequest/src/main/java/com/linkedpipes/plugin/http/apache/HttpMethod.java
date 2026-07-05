package com.linkedpipes.plugin.http.apache;

import java.net.URI;
import org.apache.http.client.methods.HttpRequestBase;

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
