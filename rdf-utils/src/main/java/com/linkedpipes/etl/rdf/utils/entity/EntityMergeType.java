package com.linkedpipes.etl.rdf.utils.entity;

public enum EntityMergeType {
    /**
     * Value is added to current value list.
     */
    LOAD,
    /**
     * Value is skipped.
     */
    SKIP,
    /**
     * Only for entities. Merge multiple objects.
     */
    MERGE
}
