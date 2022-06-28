package com.linkedpipes.plugin.xpipeline;

import java.io.IOException;
import java.net.HttpURLConnection;

class Connection implements AutoCloseable {

    protected final HttpURLConnection connection;

    public Connection(HttpURLConnection connection) {
        this.connection = connection;
    }

    public boolean requestFailed() throws IOException {
        int status = connection.getResponseCode();
        return status < 200 || status > 299;
    }

    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    public String getResponseMessage() throws IOException {
        return connection.getResponseMessage();
    }

    public void finishRequest() throws IOException {
        // This force connection to happen.
        connection.getResponseCode();
    }

    @Override
    public void close(){
        connection.disconnect();
    }

}
