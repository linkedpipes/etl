package cz.skodape.hdt.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

public class ResolveJsonTemplates {

    private static final String TEMPLATES_NODE = "templates";

    private static final String REFERENCE_NODE = "$ref";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, JsonNode> templates = new HashMap<>();

    public JsonNode resolveTemplates(JsonNode root) {
        if (root.isObject()) {
            readTemplates((ObjectNode)root);
            return applyTemplates(root);
        } else {
            return root;
        }
    }

    protected void readTemplates(ObjectNode root) {
        if (!root.has(TEMPLATES_NODE)) {
            return;
        }
        JsonNode templatesNode = root.get(TEMPLATES_NODE);
        templatesNode.fields().forEachRemaining(entry -> {
            templates.put(entry.getKey(), entry.getValue());
        });
        // Remove template node we no longer need it.
        root.remove(TEMPLATES_NODE);
    }

    protected JsonNode applyTemplates(JsonNode node) {
        if (node.isObject()) {
            if (node.has(REFERENCE_NODE)) {
                String ref = node.get(REFERENCE_NODE).asText();
                return templates.get(ref);
            }
            ObjectNode result = objectMapper.createObjectNode();
            ObjectNode objectNode = (ObjectNode) node;
            var iterator = objectNode.fields();
            while (iterator.hasNext()) {
                var entry  = iterator.next();
                result.set(entry.getKey(), applyTemplates(entry.getValue()));
            }
            return result;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            ArrayNode result = objectMapper.createArrayNode();
            for (JsonNode jsonNode : arrayNode) {
                result.add(applyTemplates(jsonNode));
            }
            return result;
        } else {
            return node;
        }
    }

}
