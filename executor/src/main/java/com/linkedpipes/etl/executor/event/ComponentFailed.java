package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;

/**
 * @author Petr Škoda
 */
public class ComponentFailed extends AbstractEvent {

    private final String componentIri;

    public Throwable cause;

    ComponentFailed(String componentIri, Throwable cause) {
        super(LINKEDPIPES.EVENTS.COMPONENT_FAILED, "Component failed.");
        this.componentIri = componentIri;
        this.cause = cause;
    }

    @Override
    public void serialize(Writer writer) {
        super.serialize(writer);
        if (componentIri != null) {
            writer.addUri(iri, LINKEDPIPES.HAS_COMPONENT, componentIri);
        }
        // Continue only if we got an exception.
        if (this.cause == null) {
            return;
        }
        // Search for first RdfException and root cause (the first exception).
        // If there is only one exception, we won't check
        // for RdfException. This would cause rdfException to be null,
        // so there won't be a cause only a rootCause.
        RdfException rdfException = null;
        Throwable rootCause = cause;
        while (rootCause.getCause() != null) {
            if (rdfException == null && rootCause instanceof RdfException) {
                rdfException = (RdfException) rootCause;
            }
            rootCause = rootCause.getCause();
        }
        // Format into a message.
        if (rdfException != null) {
            writer.addString(iri, LINKEDPIPES.EVENTS.HAS_REASON,
                    rdfException.getMessage(), "en");
        }
        if (rootCause.getMessage() == null) {
            writer.addString(iri, LINKEDPIPES.EVENTS.HAS_ROOT_EXCEPTION_MESSAGE,
                    rootCause.getClass().getSimpleName(), "en");
        } else {
            writer.addString(iri, LINKEDPIPES.EVENTS.HAS_ROOT_EXCEPTION_MESSAGE,
                    rootCause.getClass().getSimpleName() + " : " +
                            rootCause.getMessage(), "en");
        }
    }

    public String getComponentUri() {
        return componentIri;
    }

}
