package com.linkedpipes.plugin.exec.httprequest;

import java.io.IOException;
import java.net.HttpURLConnection;

class Connection implements AutoCloseable {

    protected final HttpURLConnection connection;

    public Connection(HttpURLConnection connection) {
        this.connection = connection;
    }

    public HttpURLConnection getConnection() {
        return connection;
    }

    public boolean requestFailed() throws IOException {
        int status = connection.getResponseCode();
        return status < 200 || status > 299;
    }

    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    public void finishRequest() throws IOException {
        // No operation here.
    }

    @Override
    public void close() throws Exception {
        connection.disconnect();
    }

}
