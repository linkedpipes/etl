package com.linkedpipes.etl.executor.api.v1.dataunit;

/**
 * Object that is used to connect data to components.
 */
public interface DataUnit {

    /**
     * @return Name of binding.
     */
    String getBinding();

    /**
     * @return IRI of the data unit.
     */
    String getIri();

}
