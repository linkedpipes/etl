package com.linkedpipes.plugin.extractor.oauth2token;

import com.linkedpipes.etl.executor.api.v1.LpException;

import java.util.Map;

/**
 * Provider specific part of the client credentials flow. Implementations
 * must not log the client secret.
 */
interface TokenProvider {

    /**
     * Exhaustive over the enum, so that adding a provider without wiring
     * it up here fails to compile.
     */
    static TokenProvider create(
            OAuth2TokenConfiguration.Provider provider) {
        return switch (provider) {
            case ENTRA -> new EntraTokenProvider();
        };
    }

    /**
     * Check that all properties required by this provider are set. Called
     * before any network communication.
     */
    void validateConfiguration(OAuth2TokenConfiguration configuration)
            throws LpException;

    String resolveTokenEndpoint(OAuth2TokenConfiguration configuration)
            throws LpException;

    /**
     * Form parameters of the token request, in the order they should be
     * sent.
     */
    Map<String, String> createRequestBody(
            OAuth2TokenConfiguration configuration) throws LpException;

}
