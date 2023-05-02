package com.linkedpipes.plugin.http.apache;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestConfiguration {

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

        public String contentType;

    }

    private static final int DEFAULT_CAPACITY = 2;

    public String url;

    public String method;

    public Map<String, String> headers = new HashMap<>();

    public Integer timeout;

    public Boolean contentAsBody;

    public List<Content> content = new ArrayList<>(DEFAULT_CAPACITY);

    public boolean encodeUrl = false;

}
