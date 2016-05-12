package com.linkedpipes.etl.executor.api.v1.impl;

import com.linkedpipes.etl.dpu.api.service.AfterExecution;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Petr Škoda
 */
class AfterExecutionImpl implements AfterExecution {

    private final List<CustomAction> actions = new LinkedList<>();

    @Override
    public void addAction(CustomAction function) {
        actions.add(function);
    }

    public void postExecution() {
        for (CustomAction action : actions) {
            action.call();
        }
    }

}
