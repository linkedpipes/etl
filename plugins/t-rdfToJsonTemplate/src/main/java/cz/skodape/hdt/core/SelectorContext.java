package cz.skodape.hdt.core;

import java.util.Map;

/**
 * Context given to all selectors.
 */
public class SelectorContext {

    public final Map<String, PropertySource> sources;

    public final PropertySource defaultSource;

    public SelectorContext(
            Map<String, PropertySource> sources,
            PropertySource defaultSource) {
        this.sources = sources;
        this.defaultSource = defaultSource;
    }

}
