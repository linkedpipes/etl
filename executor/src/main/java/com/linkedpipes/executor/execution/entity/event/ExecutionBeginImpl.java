package com.linkedpipes.executor.execution.entity.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.utils.core.event.AbstractEvent;
import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;

/**
 *
 * @author Škoda Petr
 */
public class ExecutionBeginImpl extends AbstractEvent {

    private final String component;

    public ExecutionBeginImpl(PipelineConfiguration execution) {
        super(LINKEDPIPES.EVENTS.EXECUTION_BEGIN, "Execution started.", "en");
        this.component = execution.getUri();
    }

    @Override
    public void write(StatementWriter writer) {
        super.write(writer);
        writer.addUri(uri, "http://cunifiedviews.opendata.cz/ontology/execution/pipeline", component);
    }

}
