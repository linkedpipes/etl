package com.linkedpipes.plugin.transformer.valueParser;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.ArrayList;
import java.util.List;

@RdfToPojo.Type(iri = ValueParserVocabulary.CONFIG)
public class ValueParserConfiguration {

    /**
     * Map the group in the regular expression to output target.
     */
    @RdfToPojo.Type(iri = ValueParserVocabulary.BINDING)
    public static class OutputBinding {

        /**
         * Name of the group.
         */
        @RdfToPojo.Property(iri = ValueParserVocabulary.HAS_GROUP)
        private String group;

        /**
         * Output target.
         */
        @RdfToPojo.Property(iri = ValueParserVocabulary.HAS_TARGET)
        private String target;

        @RdfToPojo.Property(iri = ValueParserVocabulary.HAS_TYPE)
        private String type;

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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * Predicate to load value from.
     */
    @RdfToPojo.Property(iri = ValueParserVocabulary.HAS_SOURCE)
    private String source;

    /**
     * Regular expression used to parse value.
     */
    @RdfToPojo.Property(iri = ValueParserVocabulary.HAS_REGEXP)
    private String regexp;

    /**
     * If true language tags are transfered.
     */
    @RdfToPojo.Property(iri = ValueParserVocabulary.HAS_METADATA)
    private boolean keepMetadata = false;

    /**
     * Regular expression to RDF bindings.
     */
    @RdfToPojo.Property(iri = ValueParserVocabulary.HAS_BINDING)
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
