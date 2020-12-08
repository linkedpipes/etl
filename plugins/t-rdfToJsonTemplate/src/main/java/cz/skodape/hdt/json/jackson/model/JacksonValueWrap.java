package cz.skodape.hdt.json.jackson.model;

import com.fasterxml.jackson.databind.node.ValueNode;
import cz.skodape.hdt.core.ObjectReference;

import java.util.List;

/**
 * Represent a JSON primitive value, like string, number, etc. But we need
 * to expand this into object with @type and @value, array of primitives.
 * That is why this is an object.
 */
public class JacksonValueWrap
        extends JacksonReference implements ObjectReference {

    private final ValueNode node;

    public JacksonValueWrap(List<JacksonReference> parents, ValueNode node) {
        super(parents);
        this.node = node;
    }

    public ValueNode getNode() {
        return this.node;
    }

    @Override
    public String asDebugString() {
        return "JsonValueWrap: " + node.toString();
    }

    @Override
    public boolean isObjectReference() {
        return true;
    }

    @Override
    public boolean isArrayReference() {
        return false;
    }

    @Override
    public boolean isPrimitiveReference() {
        return false;
    }

}
