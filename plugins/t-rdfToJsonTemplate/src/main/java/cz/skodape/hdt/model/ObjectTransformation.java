package cz.skodape.hdt.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration of output object.
 */
public class ObjectTransformation extends BaseTransformation {

    public Map<String, BaseTransformation> properties = new HashMap<>();

}
