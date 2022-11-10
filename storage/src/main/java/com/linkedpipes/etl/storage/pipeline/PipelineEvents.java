package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.library.pipeline.model.Pipeline;

import java.util.ArrayList;
import java.util.List;

public class PipelineEvents {

    public interface Listener {

        /**
         * Called on pipelines as they are loaded from the
         * repository upon init or after reload.
         */
        default void onPipelineLoaded(Pipeline pipeline) {
            // Do nothing.
        }

        default void onPipelineCreated(Pipeline pipeline) {
            // Do nothing.
        }

        default void onPipelineUpdated(Pipeline previous, Pipeline next) {
            // Do nothing.
        }

        default void onPipelineDeleted(Pipeline pipeline) {
            // Do nothing.
        }

        /**
         * Indicate reload of all pipelines. This mean that pipelines
         * are loaded from repository and {@link #onPipelineLoaded(Pipeline)}
         * is called on all of them.
         */
        default void onPipelineReload() {
            // Do nothing.
        }

    }

    private final List<Listener> registered = new ArrayList<>();

    public void register(Listener listener) {
        registered.add(listener);
    }

    public void onPipelineLoaded(Pipeline pipeline) {
        for (Listener listener : registered) {
            listener.onPipelineLoaded(pipeline);
        }
    }

    public void onPipelineCreated(Pipeline pipeline) {
        for (Listener listener : registered) {
            listener.onPipelineCreated(pipeline);
        }
    }

    public void onPipelineUpdated(Pipeline previous, Pipeline next) {
        for (Listener listener : registered) {
            listener.onPipelineUpdated(previous, next);
        }
    }

    public void onPipelineDeleted(Pipeline pipeline) {
        for (Listener listener : registered) {
            listener.onPipelineDeleted(pipeline);
        }
    }

    public void onPipelineReload() {
        for (Listener listener : registered) {
            listener.onPipelineReload();
        }
    }

}
