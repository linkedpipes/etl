package com.linkedpipes.etl.library.io.adapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class JsonAdapter {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private JsonAdapter() {
    }

    public static void save(File file, Object object) throws IOException {
        createParent(file);
        File swap = new File(file + ".swp");
        MAPPER.writeValue(swap, object);
        move(swap, file);
    }

    protected static void createParent(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    protected static void move(File source, File target) throws IOException {
        Files.move(
                source.toPath(), target.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    public static void saveNode(File file, JsonNode node) throws IOException {
        createParent(file);
        File swap = new File(file + ".swp");
        MAPPER.writeValue(swap, node);
        move(swap, file);
    }

    public static <T> T load(File file, Class<T> type) throws IOException {
        return MAPPER.readValue(file, type);
    }

    public static JsonNode loadNode(File file) throws IOException {
        return MAPPER.readTree(file);
    }

}
