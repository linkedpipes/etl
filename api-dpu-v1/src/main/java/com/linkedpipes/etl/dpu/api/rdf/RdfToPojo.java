package com.linkedpipes.etl.dpu.api.rdf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Škoda Petr
 */
public class RdfToPojo {

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
     * Used to bind multiple classes to the same property. Should be used together with {@link Property} annotation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Types {

        Class<?>[] classes();

    }

}
