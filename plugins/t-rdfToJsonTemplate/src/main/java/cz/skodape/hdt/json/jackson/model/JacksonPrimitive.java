package cz.skodape.hdt.json.jackson.model;

import cz.skodape.hdt.core.PrimitiveReference;

import java.util.List;

/**
 * Holds the primitive value, i.e. string value.
 */
public class JacksonPrimitive
        extends JacksonReference implements PrimitiveReference {

    private final String value;

    public JacksonPrimitive(List<JacksonReference> parents, String value) {
        super(parents);
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String asDebugString() {
        return "JsonPrimitive: " + this.value;
    }

    @Override
    public boolean isObjectReference() {
        return false;
    }

    @Override
    public boolean isArrayReference() {
        return false;
    }

    @Override
    public boolean isPrimitiveReference() {
        return true;
    }

}
