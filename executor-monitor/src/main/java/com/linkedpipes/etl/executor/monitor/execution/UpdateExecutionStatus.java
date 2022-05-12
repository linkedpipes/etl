package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.execution.overview.OverviewObject;

class UpdateExecutionStatus {

    public boolean update(Execution execution, OverviewObject overview) {
        ExecutionStatus oldStatus = execution.getStatus();
        ExecutionStatus newStatus = selectExecutionStatus(execution, overview);
        if (oldStatus == newStatus) {
            // Make sure that overview is synchronized.
            overview.setStatus(execution.getStatus().asStr());
            StatusSetter.updateOverview(execution);
            return false;
        }
        // We need to update the status in the overview.
        execution.setStatus(newStatus);
        overview.setStatus(execution.getStatus().asStr());
        StatusSetter.updateOverview(execution);
        return true;
    }

    private ExecutionStatus selectExecutionStatus(
            Execution execution,OverviewObject overview) {
        ExecutionStatus status =
                ExecutionStatus.fromIri(overview.getStatus());
        // Postpone failed and finished until the execution is finished.
        switch (status) {
            case FAILED:
                if (isFinished(overview)) {
                    return ExecutionStatus.FAILED;
                } else {
                    return ExecutionStatus.RUNNING;
                }
            case FINISHED:
                if (isFinished(overview)) {
                    return ExecutionStatus.FINISHED;
                } else {
                    return ExecutionStatus.RUNNING;
                }
            case CANCELLED:
                if (isFinished(overview)) {
                    return ExecutionStatus.CANCELLED;
                } else {
                    return ExecutionStatus.CANCELLING;
                }
            case CANCELLING:
            case RUNNING:
                return statusForRunningExecution(execution, status);
            default:
                return status;
        }
    }

    private boolean isFinished(OverviewObject overview) {
        return overview.getFinish() != null;
    }

    private ExecutionStatus statusForRunningExecution(
            Execution execution, ExecutionStatus status) {
        // No executor.
        if (!execution.isExecutor()) {
            return ExecutionStatus.DANGLING;
        }
        // No responsive executor.
        if (!execution.isExecutorResponsive()) {
            return ExecutionStatus.UNRESPONSIVE;
        }
        return status;
    }

}
