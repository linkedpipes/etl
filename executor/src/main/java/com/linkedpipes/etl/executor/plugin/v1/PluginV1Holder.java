package com.linkedpipes.etl.executor.plugin.v1;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.pipeline.Pipeline;
import com.linkedpipes.etl.executor.plugin.PluginHolder;
import com.linkedpipes.etl.executor.rdf.RdfSourceWrap;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;

public class PluginV1Holder implements PluginHolder {

    private final PluginTemplate template;

    private final Class<?> componentClass;

    public PluginV1Holder(PluginTemplate template, Class<?> componentClass) {
        this.template = template;
        this.componentClass = componentClass;
    }

    @Override
    public PluginTemplate template() {
        return template;
    }

    public PluginV1Instance createInstance(
            Pipeline pipeline, String component)
            throws ExecutorException {
        // Create instance.
        Component instance;
        try {
            instance = (Component) componentClass.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new ExecutorException(
                    "Can't create component instance class.", ex);
        }
        //
        return new PluginV1Instance(
                (SequentialExecution) instance,
                component,
                wrapPipeline(pipeline));
    }

    private RdfSourceWrap wrapPipeline(Pipeline pipeline) {
        return new RdfSourceWrap(
                pipeline.getSource(), pipeline.getPipelineGraph());
    }

}
