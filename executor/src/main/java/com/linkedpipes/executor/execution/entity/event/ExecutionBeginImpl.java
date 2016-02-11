package com.linkedpipes.executor.execution.entity.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.utils.core.event.boundary.AbstractEvent;
import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;
import com.linkedpipes.etl.executor.api.v1.event.ExecutionBegin;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;

/**
 *
 * @author Škoda Petr
 */
public class ExecutionBeginImpl extends AbstractEvent implements ExecutionBegin {

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
