package com.linkedpipes.etl.executor.api.v1.vocabulary;

public class LP_OBJECTS {

    private LP_OBJECTS() {

    }

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/configuration/";

    private static final String RESOURCE =
            "http://plugins.linkedpipes.com/resource/configuration/";

    /**
     * Object control, cause given value to inherit from the
     * previous object.
     */
    public static final String INHERIT = RESOURCE + "Inherit";

    /**
     * Object control, force value of current object to all
     * follow objects.
     */
    public static final String FORCE = RESOURCE + "Force";

    /**
     * Object control, inherit value from ancestor and force it to
     * all successors.
     */
    public static final String INHERIT_AND_FORCE =
            RESOURCE + "InheritAndForce";

    /**
     * Object control, replace value from parent if that value is not forced.
     */
    public static final String NONE = RESOURCE + "None";

    /**
     * Object control info. This value is not used by control, but rather used
     * by the object merger to indicate that certain value was forced
     * in the instance.
     */
    public static final String FORCED = RESOURCE + "Forced";

    /**
     * Type of description object.
     */
    public static final String DESCRIPTION =
            "http://plugins.linkedpipes.com/ontology/ConfigurationDescription";

    /**
     * Point to type the object describe.
     */
    public static final String HAS_DESCRIBE =
            "http://plugins.linkedpipes.com/ontology/configuration/type";

    /**
     * Description has member entities.
     */
    public static final String HAS_MEMBER = PREFIX + "member";

    /**
     * Member entity refer to value property.
     */
    public static final String HAS_PROPERTY = PREFIX + "property";

    /**
     * Member entity refer to control value.
     */
    public static final String HAS_CONTROL = PREFIX + "control";

    /**
     * If true given description member represent a complex type.
     * The complex objects are merged on the object level.
     */
    public static final String IS_COMPLEX = PREFIX + "complex";

}
