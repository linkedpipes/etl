package com.linkedpipes.etl.dataunit.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface SuppressFBWarnings {

    String[] value() default {};

    String justification() default "";

}
