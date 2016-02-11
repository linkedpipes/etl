package com.linkedpipes.etl.dpu.extensions;

import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import com.linkedpipes.etl.executor.api.v1.component.Component;

/**
 *
 * @author Škoda Petr
 */
public interface ManageableExtension {

    /**
     * Called right after the extension is created.
     *
     * @param definition
     * @param componentUri
     * @param graph
     * @throws com.linkedpipes.executor.api.v1.plugin.component.Component.InitializationFailed
     */
    public void initialize(SparqlSelect definition, String componentUri, String graph)
            throws Component.InitializationFailed;

    /**
     * Called when all extensions were created.
     *
     * @throws com.linkedpipes.executor.api.v1.plugin.component.Component.InitializationFailed
     */
    public void preExecution() throws Component.ComponentFailed;

    /**
     * Called after execution method.
     */
    public void postExecution();

}
