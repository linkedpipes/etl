package com.linkedpipes.etl.plugin.api.v2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface with plugin annotations.
 */
public interface ComponentV2 {

    /**
     * IRI of a Java plugin implementation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface IRI {

        String value();

    }

}
