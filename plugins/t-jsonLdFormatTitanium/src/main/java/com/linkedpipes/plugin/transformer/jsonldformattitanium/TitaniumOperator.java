package com.linkedpipes.plugin.transformer.jsonldformattitanium;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.api.FramingApi;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.linkedpipes.etl.executor.api.v1.LpException;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class TitaniumOperator {

    private static final Logger LOG =
            LoggerFactory.getLogger(TitaniumOperator.class);

    public void compact(File input, String contextAsString, File output)
            throws LpException {
        JsonDocument json = documentFromFile(input);
        JsonDocument context = documentFromString(contextAsString);
        JsonObject result;
        try {
            result = JsonLd.compact(json, context).get();
        } catch (JsonLdError ex) {
            throw new LpException("Can't compact JSONLD.", ex);
        }
        documentToFile(result, output);
    }

    private JsonDocument documentFromFile(File file) throws LpException {
        try (InputStream input = new FileInputStream(file)) {
            return JsonDocument.of(input);
        } catch (IOException | JsonLdError ex) {
            throw new LpException(
                    "Can't convert file '{}' to document.", file, ex);
        }
    }

    private JsonDocument documentFromString(String string) throws LpException {
        Reader reader = new StringReader(string);
        try {
            return JsonDocument.of(reader);
        } catch (JsonLdError ex) {
            throw new LpException("Can't convert string to document.", ex);
        }
    }

    private void documentToFile(JsonStructure jsonStructure, File file)
            throws LpException {
        try (var fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
             var jsonWriter = Json.createWriter(fileWriter)) {
            jsonWriter.write(jsonStructure);
        } catch (IOException ex) {
            throw new LpException("Can't write file: {}", file, ex);
        }
    }

    public void flatten(File input, File output) throws LpException {
        JsonDocument json = documentFromFile(input);
        JsonStructure result;
        try {
            result = JsonLd.flatten(json).get();
        } catch (JsonLdError ex) {
            throw new LpException("Can't compact JSONLD.", ex);
        }
        documentToFile(result, output);
    }

    public void expand(File input, File output) throws LpException {
        JsonDocument json = documentFromFile(input);
        JsonStructure result;
        try {
            result = JsonLd.expand(json).get();
        } catch (JsonLdError ex) {
            throw new LpException("Can't compact JSONLD.", ex);
        }
        documentToFile(result, output);
    }

    public void frame(
            File jsonAsFile, String frameAsString, File output)
            throws LpException {
        JsonStructure result = frameToJson(jsonAsFile, frameAsString);
        documentToFile(result, output);
    }

    private JsonStructure frameToJson(
            File jsonAsFile, String frameAsString) throws LpException {
        JsonDocument json = documentFromFile(jsonAsFile);
        JsonDocument frame = documentFromString(frameAsString);
        try {
            var framingApi = new FramingApi(json, frame);
            framingApi.omitGraph(true);
            return framingApi.get();
        } catch (JsonLdError ex) {
            throw new LpException("Can't compact JSONLD.", ex);
        }
    }

    public void frameAsArray(
            File jsonAsFile, String frameAsString,
            String contextAsString, File output)
            throws LpException {
        JsonStructure framedJson = frameToJson(jsonAsFile, frameAsString);
        Optional<JsonValue> context = valueFromString(contextAsString);
        JsonStructure result = sanitizeToArray(framedJson, context);
        documentToFile(result, output);
    }

    private Optional<JsonValue> valueFromString(
            String string) throws LpException {
        Reader reader = new StringReader(string);
        try {
            Optional<JsonStructure> structure =
                    JsonDocument.of(reader).getJsonContent();
            if (structure.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(structure.get());
        } catch (JsonLdError ex) {
            // It is not a document, we use it as a string.
        }
        String trimString = string.strip();
        if (trimString.startsWith("\"") && trimString.endsWith("\"")) {
            return Optional.of(Json.createValue(
                    trimString.substring(1, trimString.length() - 1)));
        }
        throw new LpException("Invalid context");
    }

    /**
     * When multiple entities are provided, the output is enclosed in
     * graph, so the context can be set on the root entity. We need to change
     * this so the root is an array and the context is set to each item in
     * the array.
     */
    private JsonStructure sanitizeToArray(
            JsonStructure framedJson, Optional<JsonValue> context) {
        if (!JsonUtils.isObject(framedJson)) {
            return framedJson;
        }
        JsonValue graph = framedJson.getValue("/@graph");
        if (graph == null) {
            return framedJson;
        }
        JsonArrayBuilder resultBuilder = Json.createArrayBuilder();
        JsonArray graphItems;
        try {
            graphItems = graph.asJsonArray();
        } catch (ClassCastException ex) {
            LOG.info("Can't case @graph to array.");
            return framedJson;
        }
        for (JsonValue graphItem : graphItems) {
            JsonObject graphItemObject;
            try {
                graphItemObject = graphItem.asJsonObject();
            } catch (ClassCastException ex) {
                LOG.info("Can't case @graph item to object.");
                resultBuilder.add(graphItem);
                continue;
            }
            if (context.isEmpty()) {
                resultBuilder.add(graphItemObject);
                continue;
            }
            JsonObjectBuilder objectBuilder =
                    Json.createObjectBuilder(graphItemObject);
            objectBuilder.add("@context", context.get());
            resultBuilder.add(objectBuilder.build());
        }
        return resultBuilder.build();
    }

}
