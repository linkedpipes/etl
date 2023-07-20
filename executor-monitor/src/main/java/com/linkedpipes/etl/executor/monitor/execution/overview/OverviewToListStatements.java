package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_LIST;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

/**
 * Should be used to represent an execution in execution list.
 */
public class OverviewToListStatements {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public Statements asStatements(Execution execution, JsonNode json) {
        return asStatements(execution, OverviewObject.fromJson(json));
    }

    public Statements asStatements(
            Execution execution, OverviewObject overview) {
        if (ExecutionStatus.DELETED.equals(execution.getStatus())) {
            return deletedAsStatements(execution);
        }

        IRI iri = this.valueFactory.createIRI(execution.getIri());
        StatementsBuilder builder = Statements.arrayList().builder();
        builder.setDefaultGraph(execution.getListGraph());

        builder.addType(iri, LP_EXEC.EXECUTION);
        builder.addIri(iri, LP_OVERVIEW.HAS_STATUS, overview.getStatus());

        if (overview.getDirectorySize() != null) {
            builder.add(iri, LP_EXEC.HAS_SIZE,
                    valueFactory.createLiteral(overview.getDirectorySize()));
        }

        String pipeline = overview.getPipeline();
        if (pipeline != null && !"null".equals(pipeline)) {
            builder.addIri(
                    iri, LP_OVERVIEW.HAS_PIPELINE,
                    pipeline);
        }

        if (overview.getStart() != null) {
            builder.add(iri, LP_OVERVIEW.HAS_START, overview.getStart());
        }

        if (overview.getFinish() != null) {
            builder.add(iri, LP_OVERVIEW.HAS_END, overview.getFinish());
        }

        if (overview.getProgressCurrent() != null) {
            builder.add(
                    iri, LP_OVERVIEW.HAS_PROGRESS_CURRENT,
                    overview.getProgressCurrent());
        }

        if (overview.getProgressTotal() != null) {
            builder.add(
                    iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL,
                    overview.getProgressTotal());
        }

        if (overview.getProgressTotalMap() != null) {
            builder.add(
                    iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL_MAP,
                    overview.getProgressTotalMap());
        }

        if (overview.getProgressCurrentExecuted() != null) {
            builder.add(
                    iri, LP_OVERVIEW.HAS_PROGRESS_EXECUTED,
                    overview.getProgressCurrentExecuted());
        }

        if (overview.getProgressCurrentMapped() != null) {
            builder.add(
                    iri, LP_OVERVIEW.HAS_PROGRESS_MAPPED,
                    overview.getProgressCurrentMapped());
        }

        return builder;
    }

    private Statements deletedAsStatements(Execution execution) {
        IRI iri = this.valueFactory.createIRI(execution.getIri());
        StatementsBuilder statements = Statements.arrayList().builder();
        statements.setDefaultGraph(execution.getListGraph());
        statements.addType(iri, LP_LIST.TOMBSTONE);
        return statements;
    }

}
