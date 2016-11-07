package com.linkedpipes.etl.component.test;

import com.linkedpipes.etl.component.api.service.AfterExecution;

import java.util.LinkedList;
import java.util.List;

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
