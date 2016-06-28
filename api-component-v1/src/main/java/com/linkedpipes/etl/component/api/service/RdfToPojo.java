package com.linkedpipes.etl.component.api.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For the {@link java.util.Date} the GMT time zone is used.
 *
 * @author Škoda Petr
 */
public interface RdfToPojo {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Type {

        String uri();

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Property {

        String uri();

    }

    /**
     * If used with class it indicates that the class represent a simple type,
     * like typed value of string with language tag.
     *
     * If used in annotated class it marks the property in which the
     * value should be loaded.
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    public @interface Value {

    }

    /**
     * Can be used only inside class with {@link Value} annotation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Lang {

    }

}
