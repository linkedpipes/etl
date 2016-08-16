package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Used to report that execution failed. The {@link ExecutionFinished} event
 * is still raised.
 *
 * @author Škoda Petr
 */
public class ExecutionFailed extends AbstractEvent {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutionFailed.class);

    public String reason;

    public Throwable cause;

    ExecutionFailed(String reason, Throwable cause, String type,
            String label) {
        super(type, label);
        this.reason = reason;
        this.cause = cause;
    }

    @Override
    public void serialize(Writer writer) {
        super.serialize(writer);
        writer.addString(iri, LINKEDPIPES.EVENTS.HAS_REASON, reason, "en");
        //
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        if (cause != null) {
            cause.printStackTrace(pw);
            writer.addString(iri, LINKEDPIPES.EVENTS.HAS_EXCEPTION,
                    sw.toString(), "en");
            //
            Throwable rootCause = cause;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            if (rootCause.getMessage() != null) {
                writer.addString(iri,
                        LINKEDPIPES.EVENTS.HAS_ROOT_EXCEPTION_MESSAGE,
                        rootCause.getMessage(), "en");
            } else {
                LOG.error("Missing message for ExecutionFailed.", cause);
            }
        }
    }

}
