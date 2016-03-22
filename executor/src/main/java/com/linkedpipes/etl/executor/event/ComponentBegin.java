package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;

/**
 *
 */
class ComponentBegin extends AbstractEvent
        implements com.linkedpipes.etl.executor.api.v1.event.ComponentBegin {

    private final String componentIri;

    ComponentBegin(String componentIri) {
        super(LINKEDPIPES.EVENTS.COMPONENT_BEGIN, "Component started.");
        this.componentIri = componentIri;
    }

    @Override
    public void write(StatementWriter writer) {
        super.write(writer);
        writer.addUri(iri, LINKEDPIPES.HAS_COMPONENT, componentIri);
    }

    @Override
    public String getComponentUri() {
        return componentIri;
    }

}
