package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;

public interface ExecutionObserver {

    void onMapComponentBegin(ExecutionModel.Component component);

    void onMapComponentFailed(ExecutionModel.Component component,
            LpException exception);

    void onMapComponentSuccessful(ExecutionModel.Component component);

    void onExecuteComponentInitializing(ExecutionModel.Component component);

    void onExecuteComponentFailed(ExecutionModel.Component component,
            LpException exception);

    void onExecuteComponentSuccessful(ExecutionModel.Component component);

    void onExecuteComponentCantSaveDataUnit(ExecutionModel.Component component,
            LpException exception);

    void onComponentUserCodeBegin(ExecutionModel.Component component);

    /**
     * Called if the user code failed. The onExecuteComponentFailed
     * is still called.
     */
    void onComponentUserCodeFailed(ExecutionModel.Component component,
            Throwable throwable);

    void onComponentUserCodeSuccessful(ExecutionModel.Component component);

    void onEvent(ExecutionModel.Component component, Event event);

    void onCantCreateComponentExecutor(ExecutionModel.Component component,
            LpException exception);

    void onExecutionBegin();

    void onPipelineLoaded(PipelineModel pipeline);

    void onInvalidPipeline(PipelineModel pipeline, LpException exception);

    void onCantPreparePipeline(LpException exception);

    void onObserverBeginFailed(LpException exception);

    void onDataUnitsLoadingFailed(LpException exception);

    void onComponentsLoadingFailed(LpException exception);

    void onExecutionFailedOnThrowable(Throwable exception);

    /**
     * Called at the end of the ExecutionModel, no mather the execution outcome.
     */
    void onExecutionEnd();

    void onCancelRequest();

    void onObserverEndFailed(LpException exception);

    void onComponentsExecutionModelBegin();
    
    void onComponentsExecutionModelEnd();

}
