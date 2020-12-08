package cz.skodape.hdt.json.jackson.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.skodape.hdt.core.ObjectReference;

import java.util.List;

/**
 * Represent a JSON object.
 */
public class JacksonObject extends JacksonReference implements ObjectReference {
    
    private final ObjectNode node;

    public JacksonObject(List<JacksonReference> parents, ObjectNode node) {
        super(parents);
        this.node = node;
    }

    public ObjectNode getNode() {
        return this.node;
    }

    @Override
    public String asDebugString() {
        return "JsonObjectWrap: " + node.toString();
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
