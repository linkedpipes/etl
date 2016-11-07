package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

public class ComponentFinished extends AbstractEvent {

    private final String componentIri;

    ComponentFinished(String componentIri) {
        super(LINKEDPIPES.EVENTS.COMPONENT_END, "Component completed.");
        this.componentIri = componentIri;
    }

    @Override
    public void serialize(Writer writer) {
        super.serialize(writer);
        writer.addUri(iri, LINKEDPIPES.HAS_COMPONENT, componentIri);
    }

    public String getComponentUri() {
        return componentIri;
    }

}
