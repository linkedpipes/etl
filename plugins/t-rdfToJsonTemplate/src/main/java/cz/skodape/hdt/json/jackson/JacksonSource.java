package cz.skodape.hdt.json.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import cz.skodape.hdt.core.ArrayReference;
import cz.skodape.hdt.core.MemoryReferenceSource;
import cz.skodape.hdt.core.ObjectReference;
import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.PropertySource;
import cz.skodape.hdt.core.Reference;
import cz.skodape.hdt.core.ReferenceSource;
import cz.skodape.hdt.json.jackson.model.JacksonArray;
import cz.skodape.hdt.json.jackson.model.JacksonNodeArray;
import cz.skodape.hdt.json.jackson.model.JacksonObject;
import cz.skodape.hdt.json.jackson.model.JacksonPrimitive;
import cz.skodape.hdt.json.jackson.model.JacksonReference;
import cz.skodape.hdt.json.jackson.model.JacksonReverseArray;
import cz.skodape.hdt.json.jackson.model.JacksonValueWrap;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JacksonSource implements PropertySource {

    private final URL url;

    private List<Reference> roots = new ArrayList<>();

    public JacksonSource(URL url) {
        this.url = url;
    }

    public static JacksonSource create(String content)
            throws IOException, OperationFailed {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new StringReader(content));
        JacksonSource result = new JacksonSource(null);
        result.addFromJsonNode(root);
        return result;
    }

    @Override
    public void open() throws OperationFailed {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(url);
        } catch (IOException ex) {
            throw new OperationFailed("Can't read source.", ex);
        }
        addFromJsonNode(root);
    }

    private void addFromJsonNode(JsonNode root) throws OperationFailed {
        if (root.isArray()) {
            ArrayNode nodes = (ArrayNode) root;
            this.roots = new ArrayList<>();
            for (JsonNode node : nodes) {
                this.roots.add(wrapJsonNode(node, null));
            }
        } else {
            this.roots = Collections.singletonList(wrapJsonNode(root, null));
        }
    }

    private JacksonReference wrapJsonNode(
            JsonNode node, JacksonReference parent) throws OperationFailed {
        List<JacksonReference> parents = collectParents(parent);
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            List<JsonNode> values = new ArrayList<>();
            arrayNode.elements().forEachRemaining(values::add);
            return new JacksonNodeArray(parents, values);
        } else if (node.isObject()) {
            return new JacksonObject(parents, (ObjectNode) node);
        } else if (node.isValueNode()) {
            return new JacksonValueWrap(parents, (ValueNode) node);
        }
        throw new OperationFailed("Unsupported JSON node.");
    }

    private List<JacksonReference> collectParents(JacksonReference parent) {
        if (parent == null) {
            return Collections.emptyList();
        }
        List<JacksonReference> result;
        result = new ArrayList<>(parent.getParents().size() + 1);
        result.addAll(parent.getParents());
        result.add(parent);
        return result;
    }

    @Override
    public void close() {
        this.roots.clear();
    }

    @Override
    public ReferenceSource roots() {
        return new MemoryReferenceSource<>(this.roots);
    }

    @Override
    public ReferenceSource source(Reference reference) throws OperationFailed {
        return new MemoryReferenceSource<>(sourceAsArray(reference));
    }

    public List<Reference> sourceAsArray(Reference reference)
            throws OperationFailed {
        List<Reference> result = new ArrayList<>();
        if (reference instanceof JacksonObject) {
            result.add(reference);
        } else if (reference instanceof JacksonNodeArray) {
            JacksonNodeArray nodeReference = (JacksonNodeArray) reference;
            for (JsonNode node : nodeReference.getNodes()) {
                result.add(wrapJsonNode(node, nodeReference));
            }
        } else if (reference instanceof JacksonValueWrap) {
            result.add(reference);
        } else if (reference instanceof JacksonArray) {
            JacksonReference jsonReference = (JacksonReference) reference;
            ((JacksonArray) reference)
                    .getValues()
                    .stream()
                    .map(value -> wrapString(value, jsonReference))
                    .forEach(result::add);
        } else if (reference instanceof JacksonPrimitive) {
            result.add(reference);
        } else if (reference instanceof JacksonReverseArray) {
            JacksonReverseArray reverseArray =
                    (JacksonReverseArray) reference;
            return reverseArray.getReferences();
        } else {
            if (reference == null) {
                throw new OperationFailed("Reference is null.");
            } else {
                throw new OperationFailed(
                        "Unknown reference type: {}", reference.getClass());
            }
        }
        return result;
    }

    private JacksonReference wrapString(String value, JacksonReference parent) {
        return new JacksonPrimitive(collectParents(parent), value);
    }

    @Override
    public ArrayReference property(ObjectReference reference, String property)
            throws OperationFailed {
        if (reference instanceof JacksonObject) {
            JacksonObject objectReference =
                    (JacksonObject) reference;
            JsonNode node = objectReference.getNode().get(property);
            return new JacksonNodeArray(
                    collectParents(objectReference),
                    Collections.singletonList(node));
        }
        if (reference instanceof JacksonValueWrap) {
            JacksonValueWrap valueReference = (JacksonValueWrap) reference;
            List<JacksonReference> parents = collectParents(valueReference);
            ValueNode node = valueReference.getNode();
            switch (property) {
                case "@value":
                    return new JacksonArray(parents, node.asText());
                case "@type":
                    return new JacksonArray(parents, getNodeType(node));
                default:
                    return new JacksonArray(parents);
            }
        }
        return null;
    }

    private String getNodeType(ValueNode node) throws OperationFailed {
        if (node.isNumber()) {
            return "number";
        } else if (node.isBoolean()) {
            return "boolean";
        } else if (node.isTextual()) {
            return "string";
        }
        throw new OperationFailed("Can't get ValueNode type.");
    }

    @Override
    public ArrayReference reverseProperty(Reference reference, String property)
            throws OperationFailed {
        throw new OperationFailed("Operation not supported.");
    }

}
