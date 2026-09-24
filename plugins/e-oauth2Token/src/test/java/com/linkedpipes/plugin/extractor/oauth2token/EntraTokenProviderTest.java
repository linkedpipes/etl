package com.linkedpipes.plugin.extractor.oauth2token;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class EntraTokenProviderTest {

    private final EntraTokenProvider provider = new EntraTokenProvider();

    @Test
    public void endpointIsDerivedFromTenant() throws Exception {
        Assertions.assertEquals(
                "https://login.microsoftonline.com/tenant-id"
                        + "/oauth2/v2.0/token",
                provider.resolveTokenEndpoint(createConfiguration()));
    }

    @Test
    public void endpointOverrideWins() throws Exception {
        OAuth2TokenConfiguration configuration = createConfiguration();
        configuration.setTokenEndpoint("http://localhost/token");
        Assertions.assertEquals("http://localhost/token",
                provider.resolveTokenEndpoint(configuration));
    }

    @Test
    public void requestBodyIsClientCredentialsGrant() throws Exception {
        Map<String, String> body =
                provider.createRequestBody(createConfiguration());
        Assertions.assertEquals("client_credentials", body.get("grant_type"));
        Assertions.assertEquals("client-id", body.get("client_id"));
        Assertions.assertEquals("client-secret", body.get("client_secret"));
        Assertions.assertEquals("https://graph.microsoft.com/.default",
                body.get("scope"));
        Assertions.assertEquals(4, body.size());
    }

    @Test
    public void missingTenantIsReported() {
        OAuth2TokenConfiguration configuration = createConfiguration();
        configuration.setTenant("");
        assertValidationFails(configuration, "tenant");
    }

    @Test
    public void missingTenantIsAllowedWithEndpointOverride()
            throws Exception {
        OAuth2TokenConfiguration configuration = createConfiguration();
        configuration.setTenant("");
        configuration.setTokenEndpoint("http://localhost/token");
        provider.validateConfiguration(configuration);
    }

    @Test
    public void missingClientIdIsReported() {
        OAuth2TokenConfiguration configuration = createConfiguration();
        configuration.setClientId("");
        assertValidationFails(configuration, "clientId");
    }

    @Test
    public void missingClientSecretIsReported() {
        OAuth2TokenConfiguration configuration = createConfiguration();
        configuration.setClientSecret("");
        assertValidationFails(configuration, "clientSecret");
    }

    @Test
    public void missingScopeIsReported() {
        OAuth2TokenConfiguration configuration = createConfiguration();
        configuration.setScope("");
        assertValidationFails(configuration, "scope");
    }

    private void assertValidationFails(
            OAuth2TokenConfiguration configuration, String property) {
        LpException exception = Assertions.assertThrows(LpException.class,
                () -> provider.validateConfiguration(configuration));
        Assertions.assertTrue(
                exception.getMessage().endsWith("#" + property),
                exception.getMessage());
    }

    static OAuth2TokenConfiguration createConfiguration() {
        OAuth2TokenConfiguration result = new OAuth2TokenConfiguration();
        result.setTenant("tenant-id");
        result.setClientId("client-id");
        result.setClientSecret("client-secret");
        result.setScope("https://graph.microsoft.com/.default");
        return result;
    }

}
