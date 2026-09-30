package com.linkedpipes.plugin.extractor.oauth2token;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Local token endpoint, so that tests never talk to a real tenant.
 */
class TestTokenServer implements AutoCloseable {

    private final HttpServer server;

    private String lastRequestBody;

    TestTokenServer(int statusCode, String responseBody) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(
                InetAddress.getLoopbackAddress(), 0), 0);
        this.server.createContext("/token", (exchange) -> {
            this.lastRequestBody = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(
                    statusCode, body.length == 0 ? -1 : body.length);
            if (body.length > 0) {
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        });
        this.server.start();
    }

    public String endpoint() {
        return "http://" + InetAddress.getLoopbackAddress().getHostAddress()
                + ":" + server.getAddress().getPort() + "/token";
    }

    public String lastRequestBody() {
        return lastRequestBody;
    }

    @Override
    public void close() {
        server.stop(0);
    }

}
