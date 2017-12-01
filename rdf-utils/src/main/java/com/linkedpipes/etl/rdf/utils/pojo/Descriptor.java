package com.linkedpipes.etl.rdf.utils.pojo;

import java.lang.reflect.Field;

public interface Descriptor {

    String getObjectType();

    /**
     * @return Field used to set object IRI or null.
     */
    Field getFieldForResource();

    /**
     * @param predicate
     * @return Field to set value of given property to or null to ignore.
     */
    Field getFieldForPredicate(String predicate);

}
