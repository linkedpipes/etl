package com.linkedpipes.etl.executor.api.v1.dataunit;

/**
 * Object that is used to connect data to components.
 */
public interface DataUnit {

    /**
     * Name of binding.
     */
    String getBinding();

    /**
     * IRI of the data unit in the pipeline model/configuration.
     */
    String getIri();

}
