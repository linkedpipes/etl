package com.linkedpipes.executor.execution.entity.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.utils.core.event.boundary.AbstractEvent;
import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;
import com.linkedpipes.etl.executor.api.v1.event.ComponentEnd;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;

/**
 *
 * @author Škoda Petr
 */
public class ComponentEndImpl extends AbstractEvent implements ComponentEnd {

    private final String componentUri;

    public ComponentEndImpl(PipelineConfiguration.Component component) {
        super(LINKEDPIPES.EVENTS.COMPONENT_END, "Component completed.", "en");
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
