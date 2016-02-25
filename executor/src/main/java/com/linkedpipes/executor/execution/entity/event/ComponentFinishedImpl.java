package com.linkedpipes.executor.execution.entity.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.utils.core.event.AbstractEvent;
import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;
import com.linkedpipes.etl.executor.api.v1.event.ComponentFinished;

/**
 *
 * @author Škoda Petr
 */
public class ComponentFinishedImpl extends AbstractEvent implements ComponentFinished {

    private final String componentUri;

    public ComponentFinishedImpl(PipelineConfiguration.Component component) {
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
