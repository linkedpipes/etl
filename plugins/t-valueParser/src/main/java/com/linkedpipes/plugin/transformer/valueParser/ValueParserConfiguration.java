package com.linkedpipes.plugin.transformer.valueParser;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = ValueParserVocabulary.CONFIG)
public class ValueParserConfiguration {

    /**
     * Map the group in the regular expression to output target.
     */
    @RdfToPojo.Type(uri = ValueParserVocabulary.BINDING)
    public static class OutputBinding {

        /**
         * Name of the group.
         */
        @RdfToPojo.Property(uri = ValueParserVocabulary.HAS_GROUP)
        private String group;

        /**
         * Output target.
         */
        @RdfToPojo.Property(uri = ValueParserVocabulary.HAS_TARGET)
        private String target;

        public OutputBinding() {
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    /**
     * Predicate to load value from.
     */
    @RdfToPojo.Property(uri = ValueParserVocabulary.HAS_SOURCE)
    private String source;

    /**
     * Regular expression used to parse value.
     */
    @RdfToPojo.Property(uri = ValueParserVocabulary.HAS_REGEXP)
    private String regexp;

    /**
     * If true language tags are transfered.
     */
    @RdfToPojo.Property(uri = ValueParserVocabulary.HAS_METADATA)
    private boolean keepMetadata = false;

    /**
     * Regular expression to RDF bindings.
     */
    @RdfToPojo.Property(uri = ValueParserVocabulary.HAS_BINDING)
    private List<OutputBinding> bindings = new ArrayList<>(2);

    public ValueParserConfiguration() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public boolean isKeepMetadata() {
        return keepMetadata;
    }

    public void setKeepMetadata(boolean keepMetadata) {
        this.keepMetadata = keepMetadata;
    }

    public List<OutputBinding> getBindings() {
        return bindings;
    }

    public void setBindings(List<OutputBinding> bindings) {
        this.bindings = bindings;
    }
}
