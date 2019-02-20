package com.linkedpipes.etl.executor.monitor.debug.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

class ResponseContent {

    public static final String TYPE_FILE = "file";

    public static final String TYPE_DIR = "dir";

    public static final String TYPE_AMBIGUOUS = "ambiguous";

    public static class Entry {

        public String type;

        public String name;

        public String source;

        public Long size;

        public Entry(String type, String name, String source, Long size) {
            this.type = type;
            this.name = name;
            this.source = source;
            this.size = size;
        }

    }

    public static class Metadata {

        public long count;

        public String type;

    }

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
