package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

/**
 *
 * @author Petr Škoda
 */
class ComponentFailed extends AbstractEvent
        implements com.linkedpipes.etl.executor.api.v1.event.ComponentFailed {

    private final String componentIri;

    public Throwable cause;

    ComponentFailed(String componentIri, Throwable cause) {
        super(LINKEDPIPES.EVENTS.COMPONENT_FAILED, "Component failed.");
        this.componentIri = componentIri;
        this.cause = cause;
    }

    @Override
    public void write(StatementWriter writer) {
        super.write(writer);
        writer.addUri(iri, LINKEDPIPES.HAS_COMPONENT, componentIri);
        //
        Throwable rootCause = cause;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        if (rootCause.getMessage() != null) {
            writer.addString(iri, LINKEDPIPES.EVENTS.HAS_ROOT_EXCEPTION_MESSAGE,
                    rootCause.getMessage(), "en");
        }
    }

    @Override
    public String getComponentUri() {
        return componentIri;
    }

}
