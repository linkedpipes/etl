package com.linkedpipes.executor.execution.entity.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.utils.core.event.AbstractEvent;
import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;
import com.linkedpipes.etl.executor.api.v1.event.ComponentBegin;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;

/**
 *
 * @author Škoda Petr
 */
public class ComponentBeginImpl extends AbstractEvent implements ComponentBegin {

    private final String componentUri;

    public ComponentBeginImpl(PipelineConfiguration.Component component) {
        super(LINKEDPIPES.EVENTS.COMPONENT_BEGIN, "Component started.", "en");
        this.componentUri = component.getUri();
    }

    @Override
    public void write(StatementWriter writer) {
        super.write(writer);
        writer.addUri(uri, LINKEDPIPES.HAS_COMPONENT, componentUri);
    }

    @Override
    public String getComponentUri() {
        return componentUri;
    }

}
