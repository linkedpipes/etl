package cz.skodape.hdt.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class TransformationFileAdapter {

    public interface SourceConfigurationAdapter {

        /**
         * Return null if the adapter can not read given content.
         */
        SourceConfiguration readJson(JsonNode root) throws IOException;

    }

    public interface SelectorConfigurationAdapter {

        /**
         * Return null if the adapter can not read given content.
         */
        SelectorConfiguration readJson(JsonNode root) throws IOException;

    }

    public interface OutputConfigurationAdapter {

        OutputConfiguration readJson(JsonNode root) throws IOException;

    }

    private final List<SourceConfigurationAdapter> sourceAdapters =
            new ArrayList<>();

    private final List<SelectorConfigurationAdapter> selectorAdapters =
            new ArrayList<>();

    private final List<OutputConfigurationAdapter> outputAdapters =
            new ArrayList<>();

    /**
     * Used to monitor path.
     */
    private final Stack<JsonNode> path = new Stack<>();

    public void addAdapter(SourceConfigurationAdapter adapter) {
        sourceAdapters.add(adapter);
    }

    public void addAdapter(SelectorConfigurationAdapter adapter) {
        selectorAdapters.add(adapter);
    }

    public void addAdapter(OutputConfigurationAdapter adapter) {
        outputAdapters.add(adapter);
    }

    public TransformationFile readJson(URL url) throws IOException {
        return readJson(loadJson(url));
    }

    public TransformationFile readJson(JsonNode rawRoot) throws IOException {
        JsonNode root = (new ResolveJsonTemplates()).resolveTemplates(rawRoot);
        TransformationFile result = new TransformationFile();
        result.rootSource = root.get("rootSource").asText();
        result.propertySource = root.get("propertySource").asText();
        result.sources = readSources(root.get("sources"));
        result.transformation = readTransformation(root.get("transformation"));
        return result;
    }

    protected JsonNode loadJson(URL url) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(url);
    }

    protected Map<String, SourceConfiguration> readSources(JsonNode node)
            throws IOException {
        path.push(node);
        var iterator = node.fields();
        Map<String, SourceConfiguration> result = new HashMap<>();
        while (iterator.hasNext()) {
            var next = iterator.next();
            String key = next.getKey();
            JsonNode nextNode = next.getValue();
            SourceConfiguration value = readSource(nextNode);
            result.put(key, value);
        }
        path.pop();
        return result;
    }

    protected SourceConfiguration readSource(JsonNode node) throws IOException {
        for (SourceConfigurationAdapter sourceAdapter : sourceAdapters) {
            var configuration = sourceAdapter.readJson(node);
            if (configuration == null) {
                continue;
            }
            return configuration;
        }
        throw new IOException(formatException(
                "Can't recognize source definition.", node));
    }

    protected String formatException(String message, JsonNode node) {
        return message + " For : \n"
                + node.asText() + "\nin:\n" + path.peek().asText();
    }

    protected BaseTransformation readTransformation(JsonNode node)
            throws IOException {
        if (!node.has("type")) {
            throw new IOException(formatException(
                    "Missing type.", node));
        }
        String type = node.get("type").asText();
        BaseTransformation result;
        switch (type) {
            case "object":
                path.push(node);
                result = readObjectTransformation(node);
                path.pop();
                break;
            case "array":
                path.push(node);
                result = readArrayTransformation(node);
                path.pop();
                break;
            case "primitive":
                path.push(node);
                result = readPrimitiveTransformation(node);
                path.pop();
                break;
            default:
                throw new IOException(formatException(
                        "Invalid transformation type.", node));
        }
        return result;
    }

    protected BaseTransformation readObjectTransformation(JsonNode node)
            throws IOException {
        ObjectTransformation result = new ObjectTransformation();
        result.selectors = readSelectors(node.get("selectors"));
        var iterator = node.get("properties").fields();
        while (iterator.hasNext()) {
            var next = iterator.next();
            String key = next.getKey();
            BaseTransformation value = readTransformation(next.getValue());
            result.properties.put(key, value);
        }
        return result;
    }

    protected List<SelectorConfiguration> readSelectors(JsonNode node)
            throws IOException {
        var iterator = node.iterator();
        path.add(node);
        List<SelectorConfiguration> result = new ArrayList<>();
        while (iterator.hasNext()) {
            JsonNode selectorNode = iterator.next();
            SelectorConfiguration value = readSelector(selectorNode);
            if (value == null) {
                throw new IOException(formatException(
                        "Can't read selector.", selectorNode));
            }
            result.add(value);
        }
        path.pop();
        return result;
    }

    protected SelectorConfiguration readSelector(JsonNode node)
            throws IOException {
        for (SelectorConfigurationAdapter sourceAdapter : selectorAdapters) {
            var configuration = sourceAdapter.readJson(node);
            if (configuration == null) {
                continue;
            }
            return configuration;
        }
        return null;
    }

    protected BaseTransformation readArrayTransformation(JsonNode node)
            throws IOException {
        ArrayTransformation result = new ArrayTransformation();
        result.selectors = readSelectors(node.get("selectors"));
        for (JsonNode jsonNode : node.get("items")) {
            result.items.add(readTransformation(jsonNode));
        }
        return result;
    }

    protected BaseTransformation readPrimitiveTransformation(JsonNode node)
            throws IOException {
        PrimitiveTransformation result = new PrimitiveTransformation();
        result.selectors = readSelectors(node.get("selectors"));
        if (node.has("constant")) {
            result.constantValue = node.get("constant").textValue();
        }
        if (node.has("default")) {
            result.defaultValue = node.get("default").textValue();
        }
        if (node.has("output")) {
            JsonNode output = node.get("output");
            result.outputConfiguration = readOutput(node.get("output"));
            if (result.outputConfiguration == null) {
                throw new IOException(formatException(
                        "Can't recognize output definition.", output));
            }
        }
        return result;
    }

    protected OutputConfiguration readOutput(JsonNode node) throws IOException {
        for (OutputConfigurationAdapter sourceAdapter : outputAdapters) {
            var configuration = sourceAdapter.readJson(node);
            if (configuration == null) {
                continue;
            }
            return configuration;
        }
        return null;
    }

}
