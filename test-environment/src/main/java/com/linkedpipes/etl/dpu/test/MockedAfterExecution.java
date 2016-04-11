package com.linkedpipes.etl.dpu.test;

import com.linkedpipes.etl.dpu.api.service.AfterExecution;
import com.linkedpipes.etl.dpu.api.service.AfterExecution.CustomAction;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Petr Škoda
 */
final class MockedAfterExecution implements AfterExecution {

    private final List<CustomAction> actions = new LinkedList<>();

    @Override
    public void addAction(CustomAction function) {
        actions.add(function);
    }

    /**
     * Execute all execute after functions.
     */
    public void execute() {
        for (CustomAction action : actions) {
            action.call();
        }
    }

}
