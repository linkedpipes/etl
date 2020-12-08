package cz.skodape.hdt.json.jackson.model;

import com.fasterxml.jackson.databind.JsonNode;
import cz.skodape.hdt.core.ArrayReference;

import java.util.Collections;
import java.util.List;

/**
 * Represent a array of JSON nodes. But we need to also represnt an array of
 * JsonNodes in general. To allow re-use of this class we thus store an array
 * instead of {@link com.fasterxml.jackson.databind.node.ArrayNode}.
 */
public class JacksonNodeArray
        extends JacksonReference implements ArrayReference {

    private final List<JsonNode> nodes;

    public JacksonNodeArray(
            List<JacksonReference> parents, List<JsonNode> nodes) {
        super(parents);
        this.nodes = nodes;
    }

    public List<JsonNode> getNodes() {
        return Collections.unmodifiableList(this.nodes);
    }

    @Override
    public String asDebugString() {
        return "JsonNodeArray";
    }

    @Override
    public boolean isObjectReference() {
        return false;
    }

    @Override
    public boolean isArrayReference() {
        return true;
    }

    @Override
    public boolean isPrimitiveReference() {
        return false;
    }

}
