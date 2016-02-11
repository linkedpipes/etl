package com.linkedpipes.etl.dpu.extensions;

import com.linkedpipes.etl.dpu.api.extensions.AfterExecution;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Petr Škoda
 */
public class AfterExecutionImpl implements AfterExecution, ManageableExtension {

    private final List<CustomAction> actions = new LinkedList<>();

    @Override
    public void addAction(CustomAction function) {
        actions.add(function);
    }

    @Override
    public void initialize(SparqlSelect definition, String componentUri, String graph)
            throws Component.InitializationFailed {
        // No operation here.
    }

    @Override
    public void preExecution() {
        // No operation here.
    }

    @Override
    public void postExecution() {
        for (CustomAction action : actions) {
            action.call();
        }
    }

}
