package cz.skodape.hdt.json.jackson.model;

import cz.skodape.hdt.core.Reference;

import java.util.Collections;
import java.util.List;

/**
 * Base class for reference objects, we keep our parent as JSON nodes
 * can have only one. That make reverse property functionality easier.
 */
public abstract class JacksonReference implements Reference {

    private final List<JacksonReference> parents;
    
    protected JacksonReference(List<JacksonReference> parents) {
        this.parents = parents;
    }

    public List<JacksonReference> getParents() {
        return Collections.unmodifiableList(this.parents);
    }

}
