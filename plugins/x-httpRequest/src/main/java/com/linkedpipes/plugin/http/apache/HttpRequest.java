package com.linkedpipes.plugin.http.apache;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    public static class Content {

        /**
         * Reference to a file to send.
         */
        public File file;

        /**
         * String value to send.
         */
        public String value;

        /**
         * Name provided for multipart request.
         */
        public String name;

        /**
         * File name provided for multipart request.
         */
        public String fileName;

        /**
         * Content type for this content.
         */
        public String contentType;

    }

    private static final int DEFAULT_CAPACITY = 2;

    /**
     * URL to execute the initial request to.
     */
    public String url;

    public boolean encodeUrl = false;

    /**
     * We try to do our best to follow redirects.
     * We can still follow some due to used library.
     */
    public boolean followRedirect = false;

    /**
     * HTTP method.
     */
    public String method;

    /**
     * Headers.
     */
    public Map<String, String> headers = new HashMap<>();

    public Integer timeout;

    /**
     * If true sends the content as a body.
     */
    public boolean contentAsBody;

    public List<Content> content = new ArrayList<>(DEFAULT_CAPACITY);

}
