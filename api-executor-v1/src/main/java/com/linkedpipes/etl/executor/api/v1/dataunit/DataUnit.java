package com.linkedpipes.etl.executor.api.v1.dataunit;

/**
 *
 * @author Škoda Petr
 */
public interface DataUnit {

    /**
     *
     * @return Name of binding.
     */
    public String getBinding();

    /**
     *
     * @return URI of the data unit.
     */
    public String getResourceUri();

    /**
     *
     * @return True if DataUnit has been initialized.
     */
    public boolean isInitialized();

}
