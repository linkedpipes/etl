package cz.skodape.hdt.model;

/**
 * Configuration of output primitive value.
 */
public class PrimitiveTransformation extends BaseTransformation {

    public String constantValue = null;

    public String defaultValue = null;

    /**
     * Optional configuration for the output, may contain for example
     * definition of the data type.
     */
    public OutputConfiguration outputConfiguration = null;

}
