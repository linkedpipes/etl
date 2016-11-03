package com.linkedpipes.etl.component.api.service;

/**
 * Can be used to perform clean up actions after the execution method,
 * regardless of execution method outcome.
 *
 * The intended usage is to close opened resources that were open during
 * execution and whose closing would render code unreadable.
 */
public interface AfterExecution {

    @FunctionalInterface
    public static interface CustomAction {

        public void call();

    }

    void addAction(CustomAction function);

}
