package com.linkedpipes.plugin.extractor.oauth2token;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;

public class TokenServiceTest {

    private static final String SECRET = "client-secret";

    private final TokenService service =
            new TokenService(new EntraTokenProvider());

    @Test
    public void successfulResponseIsParsed() throws Exception {
        try (TestTokenServer server = new TestTokenServer(200,
                "{\"access_token\":\"the-token\","
                        + "\"token_type\":\"Bearer\","
                        + "\"expires_in\":3599}")) {
            TokenResponse response =
                    service.requestToken(configuration(server.endpoint()));
            Assertions.assertEquals("the-token", response.accessToken());
            Assertions.assertEquals("Bearer", response.tokenType());
            Assertions.assertEquals(3599, response.expiresIn());
            String body = server.lastRequestBody();
            Assertions.assertTrue(
                    body.contains("grant_type=client_credentials"), body);
            Assertions.assertTrue(body.contains("client_id=client-id"), body);
        }
    }

    @Test
    public void tildeInSecretIsEscaped() throws Exception {
        try (TestTokenServer server = new TestTokenServer(
                200, "{\"access_token\":\"the-token\"}")) {
            OAuth2TokenConfiguration configuration =
                    configuration(server.endpoint());
            configuration.setClientSecret("ab~cd");
            service.requestToken(configuration);
            Assertions.assertTrue(
                    server.lastRequestBody().contains(
                            "client_secret=ab%7Ecd"),
                    server.lastRequestBody());
        }
    }

    @Test
    public void reservedCharactersInSecretAreEscaped() throws Exception {
        try (TestTokenServer server = new TestTokenServer(
                200, "{\"access_token\":\"the-token\"}")) {
            OAuth2TokenConfiguration configuration =
                    configuration(server.endpoint());
            configuration.setClientSecret("a/b+c=d");
            service.requestToken(configuration);
            Assertions.assertTrue(
                    server.lastRequestBody().contains(
                            "client_secret=a%2Fb%2Bc%3Dd"),
                    server.lastRequestBody());
        }
    }

    @Test
    public void optionalMembersMayBeAbsent() throws Exception {
        try (TestTokenServer server = new TestTokenServer(200,
                "{\"access_token\":\"the-token\"}")) {
            TokenResponse response =
                    service.requestToken(configuration(server.endpoint()));
            Assertions.assertEquals("the-token", response.accessToken());
            Assertions.assertNull(response.tokenType());
            Assertions.assertNull(response.expiresIn());
        }
    }

    @Test
    public void nonSuccessStatusFails() throws Exception {
        try (TestTokenServer server = new TestTokenServer(401,
                "{\"error\":\"invalid_client\"}")) {
            LpException exception = requestAndExpectFailure(server);
            Assertions.assertTrue(
                    exception.getMessage().contains("401"),
                    exception.getMessage());
            Assertions.assertFalse(
                    exception.getMessage().contains(SECRET),
                    "the client secret must not reach the message");
        }
    }

    @Test
    public void errorResponseIsReported() throws Exception {
        try (TestTokenServer server = new TestTokenServer(401,
                "{\"error\":\"invalid_client\",\"error_description\""
                        + ":\"AADSTS7000215: Invalid client secret "
                        + "provided.\"}")) {
            LpException exception = requestAndExpectFailure(server);
            Assertions.assertTrue(
                    exception.getMessage().contains("invalid_client"),
                    exception.getMessage());
            Assertions.assertTrue(
                    exception.getMessage().contains("AADSTS7000215"),
                    exception.getMessage());
            Assertions.assertFalse(
                    exception.getMessage().contains(SECRET),
                    "the client secret must not reach the message");
        }
    }

    @Test
    public void nonJsonErrorResponseStillReportsStatus() throws Exception {
        try (TestTokenServer server = new TestTokenServer(
                503, "<html>gateway error</html>")) {
            LpException exception = requestAndExpectFailure(server);
            Assertions.assertTrue(
                    exception.getMessage().contains("503"),
                    exception.getMessage());
            Assertions.assertTrue(
                    exception.getMessage().contains("(not provided)"),
                    exception.getMessage());
        }
    }

    @Test
    public void missingAccessTokenFails() throws Exception {
        try (TestTokenServer server = new TestTokenServer(
                200, "{\"token_type\":\"Bearer\"}")) {
            Assertions.assertEquals(
                    "Token response does not contain an access token.",
                    requestAndExpectFailure(server).getMessage());
        }
    }

    @Test
    public void blankAccessTokenFails() throws Exception {
        try (TestTokenServer server = new TestTokenServer(
                200, "{\"access_token\":\"  \"}")) {
            Assertions.assertEquals(
                    "Token response does not contain an access token.",
                    requestAndExpectFailure(server).getMessage());
        }
    }

    @Test
    public void malformedResponseFails() throws Exception {
        try (TestTokenServer server = new TestTokenServer(
                200, "this is not JSON")) {
            Assertions.assertEquals("Can't parse token response.",
                    requestAndExpectFailure(server).getMessage());
        }
    }

    @Test
    public void nonObjectResponseFails() throws Exception {
        try (TestTokenServer server = new TestTokenServer(200, "[]")) {
            Assertions.assertEquals("Can't parse token response.",
                    requestAndExpectFailure(server).getMessage());
        }
    }

    @Test
    public void emptyResponseFails() throws Exception {
        try (TestTokenServer server = new TestTokenServer(200, "")) {
            Assertions.assertEquals("Can't parse token response.",
                    requestAndExpectFailure(server).getMessage());
        }
    }

    @Test
    public void networkErrorFails() throws Exception {
        String endpoint = "http://127.0.0.1:" + unusedPort() + "/token";
        LpException exception = Assertions.assertThrows(LpException.class,
                () -> service.requestToken(configuration(endpoint)));
        Assertions.assertTrue(
                exception.getMessage().startsWith(
                        "Can't request access token from:"),
                exception.getMessage());
        Assertions.assertFalse(exception.getMessage().contains(SECRET),
                "the client secret must not reach the message");
    }

    @Test
    public void clientSecretIsNeverLogged() throws Exception {
        Logger root = (Logger) LoggerFactory.getLogger(
                Logger.ROOT_LOGGER_NAME);
        Level originalLevel = root.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        root.setLevel(Level.TRACE);
        root.addAppender(appender);
        try {
            try (TestTokenServer server = new TestTokenServer(200,
                    "{\"access_token\":\"the-token\"}")) {
                service.requestToken(configuration(server.endpoint()));
            }
            try (TestTokenServer server = new TestTokenServer(401, "")) {
                requestAndExpectFailure(server);
            }
        } finally {
            root.detachAppender(appender);
            root.setLevel(originalLevel);
            appender.stop();
        }
        Assertions.assertFalse(appender.list.isEmpty(),
                "expected the endpoint and status to be logged");
        for (ILoggingEvent event : appender.list) {
            Assertions.assertFalse(
                    event.getFormattedMessage().contains(SECRET),
                    event.getFormattedMessage());
        }
    }

    private LpException requestAndExpectFailure(TestTokenServer server) {
        return Assertions.assertThrows(LpException.class,
                () -> service.requestToken(configuration(server.endpoint())));
    }

    private static int unusedPort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    static OAuth2TokenConfiguration configuration(String endpoint) {
        OAuth2TokenConfiguration result =
                EntraTokenProviderTest.createConfiguration();
        result.setTokenEndpoint(endpoint);
        return result;
    }

}
