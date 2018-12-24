package com.linkedpipes.etl.executor.rdf.entity;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

import java.util.List;

/**
 * Control instance for entity of given type.
 *
 * <p>The control should be loaded before use (by the factory method)
 * and thus should have prior knowledge which field should be loaded.
 */
public interface MergeControl {

    /**
     * Load entities into the control.
     */
    void init(List<EntityReference> references) throws RdfUtilsException;

    /**
     * Called at start of every reference.
     *
     * @param resource This values have been given
     * @param graph    Graph.
     */
    void onReference(String resource, String graph)
            throws RdfUtilsException;

    /**
     * Called to decide what to do with a property.
     *
     * @param property Name of a property.
     * @return Decision what to do with the property.
     */
    MergeType onProperty(String property) throws RdfUtilsException;

}
