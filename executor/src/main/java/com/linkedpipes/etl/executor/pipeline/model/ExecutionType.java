package com.linkedpipes.etl.executor.pipeline.model;

public enum ExecutionType {
    /**
     * Execute component.
     */
    EXECUTE,
    /**
     * Skip the execution and loading of data unit, in
     * fact this behave as if there was no component mentioned.
     */
    SKIP,
    /**
     * PipelineComponent is mapped, so it's not executed
     * only the data units are loaded.
     */
    MAP
}
