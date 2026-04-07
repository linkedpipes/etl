package com.linkedpipes.plugin.ehttpgetfile;

import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Request configuration.
 *
 * @param manualRedirect Use manual redirect instead of the build in {@link HttpURLConnection}.
 *                       This is to deal with <a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4620571">4620571</a>,
 *                       where by default no cross protocol redirect, e.g. HTTP to HTTPS would work.
 * @param logDetail Log additional connection detail, use for debugging.
 * @param encodeUrl Encode URL to use only ASCII characters.
 * @param useUtf8ForRedirect
 */
public record DownloaderRequest(
        Map<String, String> requestHeaders,
        Integer timeout,
        boolean manualRedirect,
        boolean logDetail,
        boolean encodeUrl,
        boolean useUtf8ForRedirect
) {
}
