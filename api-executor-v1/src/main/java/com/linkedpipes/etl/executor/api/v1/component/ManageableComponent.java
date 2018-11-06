package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.RuntimeConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;

import java.util.Map;

public interface ManageableComponent {

    /**
     * Provide component with access to data units, so it can initialize
     * its binding.
     */
    void initialize(Map<String, DataUnit> dataUnits, Component.Context context)
            throws LpException;

    /**
     * Load configuration for a component.
     *
     * @param definition Access to configuration.
     */
    void loadConfiguration(RdfSource definition) throws LpException;

    /**
     * Return null if there is no runtime configuration.
     */
    RuntimeConfiguration getRuntimeConfiguration() throws LpException;

}
