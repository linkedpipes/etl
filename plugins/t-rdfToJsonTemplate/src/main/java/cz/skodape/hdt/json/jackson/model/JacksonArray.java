package cz.skodape.hdt.json.jackson.model;

import cz.skodape.hdt.core.ArrayReference;

import java.util.Collections;
import java.util.List;

/**
 * Just to allow the @value and @type for {@link JacksonValueWrap} to be arrays.
 */
public class JacksonArray extends JacksonReference implements ArrayReference {

    private final List<String> values;

    public JacksonArray(List<JacksonReference> parents) {
        super(parents);
        this.values = Collections.emptyList();
    }

    public JacksonArray(List<JacksonReference> parents, String value) {
        super(parents);
        this.values = Collections.singletonList(value);
    }

    public JacksonArray(List<JacksonReference> parents, List<String> values) {
        super(parents);
        this.values = Collections.unmodifiableList(values);
    }

    public List<String> getValues() {
        return this.values;
    }

    @Override
    public String asDebugString() {
        return "JsonArray: " + String.join(",", this.values);
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
