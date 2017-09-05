package com.linkedpipes.etl.executor.execution.model;

public class ExecutionStatusMonitor {

    private ExecutionStatus status = ExecutionStatus.QUEUED;

    private boolean failed = false;

    public ExecutionStatus getStatus() {
        return status;
    }

    public void onMapComponentFailed() {
        failed = true;
    }

    public void onExecuteComponentFailed() {
        failed = true;
    }

    public void onExecuteComponentCantSaveDataUnit() {
        failed = true;
    }

    public void onCantCreateComponentExecutor() {
        failed = true;
    }

    public void onPipelineLoaded() {
        status = ExecutionStatus.RUNNING;
    }

    public void onInvalidPipeline() {
        failed = true;
    }

    public void onCantPreparePipeline() {
        failed = true;
    }

    public void onObserverBeginFailed() {
        failed = true;
    }

    public void onDataUnitsLoadingFailed() {
        failed = true;
    }

    public void onComponentsLoadingFailed() {
        failed = true;
    }

    public void onExecutionFailedOnThrowable() {
        failed = true;
    }

    public void onExecutionEnd() {
        if (failed) {
            status = ExecutionStatus.FAILED;
            return;
        }
        switch (status) {
            case CANCELLING:
                status = ExecutionStatus.CANCELLED;
                break;
            default:
                status = ExecutionStatus.FINISHED;
                break;
        }
    }

    public void onCancelRequest() {
        status = ExecutionStatus.CANCELLING;
    }

    public void onObserverEndFailed() {
        failed = true;
    }

    public void onComponentsExecutionBegin() {
        status = ExecutionStatus.RUNNING;
    }

}
