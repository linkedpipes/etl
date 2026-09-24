package com.linkedpipes.plugin.extractor.oauth2token;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Provider independent part of the client credentials flow.
 *
 * <p>This uses the JDK HTTP client and not Apache HttpClient as the other
 * plugins do. Apache HttpClient logs every request body to
 * org.apache.http.wire at DEBUG, and the executor ships with a DEBUG root
 * logger, which would write the client secret into the execution log.
 *
 * <p>For the same reason neither the request body nor the response body is
 * logged here, as the former holds the client secret and the latter the
 * access token.
 */
class TokenService {

    private static final Logger LOG =
            LoggerFactory.getLogger(TokenService.class);

    /**
     * Source snippets in parser error messages could carry a part of the
     * access token into the execution log.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper(
            JsonFactory.builder()
                    .disable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                    .build());

    private final TokenProvider provider;

    public TokenService(TokenProvider provider) {
        this.provider = provider;
    }

    public TokenResponse requestToken(
            OAuth2TokenConfiguration configuration) throws LpException {
        provider.validateConfiguration(configuration);
        String endpoint = provider.resolveTokenEndpoint(configuration);
        LOG.info("Requesting access token from: {}", endpoint);
        HttpRequest request = createRequest(
                endpoint, provider.createRequestBody(configuration));
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpResponse<InputStream> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofInputStream());
            return handleResponse(endpoint, response);
        } catch (IOException ex) {
            throw new LpException(
                    "Can't request access token from: {}", endpoint, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new LpException(
                    "Interrupted while requesting access token from: {}",
                    endpoint, ex);
        }
    }

    private HttpRequest createRequest(
            String endpoint, Map<String, String> body) throws LpException {
        try {
            return HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type",
                            "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            encodeBody(body), StandardCharsets.UTF_8))
                    .build();
        } catch (IllegalArgumentException ex) {
            throw new LpException("Invalid token endpoint: {}", endpoint, ex);
        }
    }

    private static String encodeBody(Map<String, String> body) {
        StringJoiner result = new StringJoiner("&");
        body.forEach((name, value) -> result.add(
                URLEncoder.encode(name, StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(value, StandardCharsets.UTF_8)));
        return result.toString();
    }

    private TokenResponse handleResponse(
            String endpoint, HttpResponse<InputStream> response)
            throws LpException {
        int statusCode = response.statusCode();
        LOG.info("Response code: {}", statusCode);
        try (InputStream stream = response.body()) {
            if (statusCode < 200 || statusCode >= 300) {
                throw failedRequest(statusCode, endpoint, stream);
            }
            return parseResponse(stream);
        } catch (IOException ex) {
            throw new LpException(
                    "Can't read token response from: {}", endpoint, ex);
        }
    }

    /**
     * A failed response carries no access token, only the error members
     * of RFC 6749. Only those two members are read, so that nothing else
     * from the response can reach the log.
     */
    private LpException failedRequest(
            int statusCode, String endpoint, InputStream stream) {
        String error = "(not provided)";
        String description = "(not provided)";
        try {
            JsonNode root = MAPPER.readTree(stream);
            if (root != null && root.isObject()) {
                error = asText(root.get("error"), error);
                description = asText(
                        root.get("error_description"), description);
            }
        } catch (IOException ex) {
            // The error body is optional, report the status without it.
        }
        return new LpException(
                "Token request failed, status: {} endpoint: {}"
                        + " error: {} description: {}",
                statusCode, endpoint, error, description);
    }

    private TokenResponse parseResponse(InputStream stream)
            throws LpException {
        JsonNode root;
        try {
            root = MAPPER.readTree(stream);
        } catch (IOException ex) {
            throw new LpException("Can't parse token response.", ex);
        }
        if (root == null || !root.isObject()) {
            throw new LpException("Can't parse token response.");
        }
        JsonNode accessToken = root.get("access_token");
        if (accessToken == null || !accessToken.isTextual()
                || accessToken.asText().isBlank()) {
            throw new LpException(
                    "Token response does not contain an access token.");
        }
        return new TokenResponse(
                accessToken.asText(),
                asText(root.get("token_type")),
                asInteger(root.get("expires_in")));
    }

    private static String asText(JsonNode node) {
        return asText(node, null);
    }

    private static String asText(JsonNode node, String defaultValue) {
        if (node == null || !node.isTextual()) {
            return defaultValue;
        }
        return node.asText();
    }

    private static Integer asInteger(JsonNode node) {
        if (node == null || !node.canConvertToInt()) {
            return null;
        }
        return node.asInt();
    }

}
