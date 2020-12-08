package cz.skodape.hdt.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Root configuration file for a transformation, defines all but output.
 */
public class TransformationFile {

    public BaseTransformation transformation;

    public String rootSource;

    public String propertySource;

    public Map<String, SourceConfiguration> sources = new HashMap<>();

}
