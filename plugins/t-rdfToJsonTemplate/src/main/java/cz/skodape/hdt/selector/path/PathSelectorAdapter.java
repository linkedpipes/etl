package cz.skodape.hdt.selector.path;

import com.fasterxml.jackson.databind.JsonNode;
import cz.skodape.hdt.model.SelectorConfiguration;
import cz.skodape.hdt.model.TransformationFileAdapter;

public class PathSelectorAdapter
        implements TransformationFileAdapter.SelectorConfigurationAdapter {

    private static final String TYPE = "Path";

    @Override
    public SelectorConfiguration readJson(JsonNode root) {
        String type = root.get("type").asText();
        if (TYPE.equals(type)) {
            return readConfiguration(root);
        }
        return null;
    }

    public PathSelectorConfiguration readConfiguration(JsonNode root) {
        PathSelectorConfiguration result = new PathSelectorConfiguration();
        for (JsonNode node : root.get("path")) {
            result.path.add(readJsonPath(node));
        }
        return result;
    }

    private PathSelectorConfiguration.Path readJsonPath(JsonNode node) {
        PathSelectorConfiguration.Path result =
                new PathSelectorConfiguration.Path();
        if (node.isObject()) {
            result.predicate = node.get("predicate").asText();
            if (node.has("reverse")) {
                result.reverse = node.get("reverse").asBoolean();
            }
        } else if (node.isTextual()) {
            result.predicate = node.textValue();
        } else {
            throw new RuntimeException(
                    "Invalid configuration :" + node.asText());
        }
        return result;
    }

}
