package com.linkedpipes.etl.executor.api.v1.rdf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that describe process of loading an RDF into the Java POJO
 * object.
 */
public class RdfToPojo {

    private RdfToPojo() {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Type {

        String iri();

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Resource {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Property {

        String iri();

        /**
         * Alternative IRIs for given predicate.
         */
        String[] alternatives() default {};

    }

}
