package com.linkedpipes.etl.executor.monitor.execution;

import java.io.File;

class ExecutionMigration {

    public boolean shouldMigrate(Execution execution) {
        File executionFile = new File(
                execution.getDirectory(), "execution.jsonld");
        File overviewFile = new File(
                execution.getDirectory(), "execution-overview.jsonld");

        return execution.getStatus() == ExecutionStatus.QUEUED
                && executionFile.exists()
                && !overviewFile.exists();
    }

    public void migrate(Execution execution) {
        StatusSetter.setStatus(execution, ExecutionStatus.INVALID);
    }

}
