package com.linkedpipes.etl.rdf.utils.entity;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

import java.util.List;

/**
 * Control instance for entity of given type.
 *
 * The control should be loaded before use (by the factory method)
 * and thus should have prior knowledge which field should be loaded.
 */
public interface EntityControl {

    /**
     * Load entities into the control.
     *
     * @param references
     */
    void init(List<EntityReference> references) throws RdfUtilsException;

    /**
     * Called at start of every reference.
     *
     * @param resource This values have been given
     * @param graph
     */
    void onReference(String resource, String graph)
            throws RdfUtilsException;

    /**
     * @param property
     * @return Decision what to do with the property.
     */
    EntityMergeType onProperty(String property) throws RdfUtilsException;

}
