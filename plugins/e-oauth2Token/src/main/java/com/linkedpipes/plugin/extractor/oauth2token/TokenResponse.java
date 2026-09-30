package com.linkedpipes.plugin.extractor.oauth2token;

/**
 * Standard client credentials response as defined by RFC 6749. The
 * access token must never be logged.
 */
record TokenResponse(
        String accessToken,
        String tokenType,
        Integer expiresIn
) {

}
