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

        /**
         * Relative path, this is used for ambiguous entries. For example
         * the entry may represent two ambiguous files and a directory. In
         * such a case the metadata entry must be ambiguous, yet
         * it is not clear how to navigate to the listed entries. As the
         * name may lead to an invalid path. This is solved using
         * this property as it may be empty.
         */
        public String path;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String source;

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

        public Entry(String type, String name, String path, String source) {
            this.type = type;
            this.name = name;
            this.path = path;
            this.source = source;
            this.size = null;
            this.mimeType = null;
        }

        public Entry(
                String type, String name, String path, String source,
                Long size, String mimeType, String publicDataPath) {
            this.type = type;
            this.name = name;
            this.path = path;
            this.source = source;
            this.size = size;
            this.mimeType = mimeType;
            this.publicDataPath = publicDataPath;
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
