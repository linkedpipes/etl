package com.linkedpipes.etl.executor.api.v1.dataunit;

/**
 * Object that is used to connect data to components.
 *
 * @author Škoda Petr
 */
public interface DataUnit {

    /**
     * @return Name of binding.
     */
    public String getBinding();

    /**
     * @return IRI of the data unit.
     */
    public String getResourceIri();

    /**
     * @return True if DataUnit has been initialized.
     */
    public boolean isInitialized();

}
