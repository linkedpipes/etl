package cz.skodape.hdt.json.jackson.model;

import cz.skodape.hdt.core.ArrayReference;
import cz.skodape.hdt.core.Reference;

import java.util.Collections;
import java.util.List;

/**
 * Used to store results of reverse property lookup. As an artificial object
 * it does not have a parent.
 */
public class JacksonReverseArray implements ArrayReference {
    
    private List<Reference> references;

    public JacksonReverseArray() {
        this.references = Collections.emptyList();
    }

    public JacksonReverseArray(JacksonReference reference) {
        this.references = Collections.singletonList(reference);
    }

    public List<Reference> getReferences() {
        return this.references;
    }
    
    @Override
    public String asDebugString() {
        return "JsonNodeReverseArray";
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
