package com.linkedpipes.plugin.extractor.oauth2token;

import com.linkedpipes.etl.executor.api.v1.LpException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Microsoft Azure Entra ID, using the v2.0 token endpoint.
 */
class EntraTokenProvider implements TokenProvider {

    private static final String ENDPOINT_TEMPLATE
            = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";

    @Override
    public void validateConfiguration(OAuth2TokenConfiguration configuration)
            throws LpException {
        if (isBlank(configuration.getTokenEndpoint())
                && isBlank(configuration.getTenant())) {
            throw new LpException("Missing property: {}",
                    OAuth2TokenVocabulary.HAS_TENANT);
        }
        if (isBlank(configuration.getClientId())) {
            throw new LpException("Missing property: {}",
                    OAuth2TokenVocabulary.HAS_CLIENT_ID);
        }
        if (isBlank(configuration.getClientSecret())) {
            throw new LpException("Missing property: {}",
                    OAuth2TokenVocabulary.HAS_CLIENT_SECRET);
        }
        if (isBlank(configuration.getScope())) {
            throw new LpException("Missing property: {}",
                    OAuth2TokenVocabulary.HAS_SCOPE);
        }
    }

    @Override
    public String resolveTokenEndpoint(
            OAuth2TokenConfiguration configuration) {
        if (!isBlank(configuration.getTokenEndpoint())) {
            return configuration.getTokenEndpoint().trim();
        }
        return String.format(ENDPOINT_TEMPLATE, URLEncoder.encode(
                configuration.getTenant().trim(), StandardCharsets.UTF_8));
    }

    @Override
    public Map<String, String> createRequestBody(
            OAuth2TokenConfiguration configuration) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("grant_type", "client_credentials");
        result.put("client_id", configuration.getClientId().trim());
        result.put("client_secret", configuration.getClientSecret());
        result.put("scope", configuration.getScope().trim());
        return result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
