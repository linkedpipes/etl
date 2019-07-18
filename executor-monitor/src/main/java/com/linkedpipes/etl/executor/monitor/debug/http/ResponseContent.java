package com.linkedpipes.etl.executor.monitor.debug.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.List;

class ResponseContent {

    public static final String TYPE_FILE = "file";

    public static final String TYPE_DIR = "dir";

    public static final String TYPE_AMBIGUOUS = "ambiguous";

    @SuppressFBWarnings
    public static class Entry {

        public String type;

        public String name;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String source;

        // Used for file type.
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public Long size;

        // Used for file type.
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String mimeType;

        public Entry(String type, String name, String source) {
            this.type = type;
            this.name = name;
            this.source = source;
            this.size = null;
            this.mimeType = null;
        }

        public Entry(
                String type, String name, String source,
                Long size, String mimeType) {
            this.type = type;
            this.name = name;
            this.source = source;
            this.size = size;
            this.mimeType = mimeType;
        }

    }

    @SuppressFBWarnings
    public static class Metadata {

        public String type;

        /**
         * Used for directories.
          */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public Long count;

        /**
         * Used for file type.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public Long size;

        /**
         * Used for file type.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String mimeType;

        /**
         * Used for file type.
         * Path that can be used to access data.
          */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String publicDataPath;

    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<Entry> data;

    public Metadata metadata = new Metadata();

    public ResponseContent(List<Entry> data) {
        this.data = data;
    }

    public String asJsonString() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}
