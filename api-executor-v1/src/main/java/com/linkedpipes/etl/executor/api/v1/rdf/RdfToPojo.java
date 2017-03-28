package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.rdf.utils.pojo.RdfLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that describe process of loading an RDF into the Java POJO
 * object.
 *
 * Implementation specifications:
 * <ul>
 * <li>For the {@link java.util.Date} the GMT time zone is used.</li>
 * </ul>
 */
public interface RdfToPojo {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Type {

        String iri();

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Property {

        String iri();

        /**
         * @return Alternative IRIs for given predicate.
         */
        String[] alternatives() default {};

    }

    /**
     * Can be used to load language string with a language tag.
     */
    class LangString implements RdfLoader.LangString {

        private String value;

        private String language;

        public LangString() {
        }

        public LangString(String value, String language) {
            this.value = value;
            this.language = language;
        }

        @Override
        public void setValue(String value, String language) {
            this.value = value;
            this.language = language;
        }

        public String getValue() {
            return value;
        }

        public String getLanguage() {
            return language;
        }

    }

}
