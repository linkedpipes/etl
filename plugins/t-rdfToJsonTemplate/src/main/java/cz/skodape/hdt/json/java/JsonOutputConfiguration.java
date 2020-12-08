package cz.skodape.hdt.json.java;

import cz.skodape.hdt.model.OutputConfiguration;

public class JsonOutputConfiguration implements OutputConfiguration {

    public enum Type {
        String,
        Number,
        Boolean;
    }

    /**
     * Define output JSON value type.
     */
    public Type datatype;

    public JsonOutputConfiguration() {
    }

    public JsonOutputConfiguration(Type datatype) {
        this.datatype = datatype;
    }

}
