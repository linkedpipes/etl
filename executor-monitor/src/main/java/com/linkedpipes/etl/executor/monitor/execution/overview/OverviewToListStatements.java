package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_LIST;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Should be used to represent an execution in execution list.
 */
public class OverviewToListStatements {

    private static final Logger LOG = LoggerFactory.getLogger(OverviewToListStatements.class);

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
        Statements statements = Statements.arrayList();
        statements.setDefaultGraph(execution.getListGraph());

        statements.addIri(iri, RDF.TYPE, LP_EXEC.EXECUTION);
        statements.addIri(iri, LP_OVERVIEW.HAS_STATUS, overview.getStatus());

        if (overview.getDirectorySize() != null) {
            statements.add(iri, LP_EXEC.HAS_SIZE,
                    valueFactory.createLiteral(overview.getDirectorySize()));
        }

        String pipeline = overview.getPipeline();
        if (pipeline != null && !"null".equals(pipeline)) {
            statements.addIri(
                    iri, LP_OVERVIEW.HAS_PIPELINE,
                    pipeline);
        }

        if (overview.getStart() != null) {
            statements.addDate(iri, LP_OVERVIEW.HAS_START, overview.getStart());
        }

        if (overview.getFinish() != null) {
            statements.addDate(iri, LP_OVERVIEW.HAS_END, overview.getFinish());
        }

        if (overview.getProgressCurrent() != null) {
            statements.addInt(
                    iri, LP_OVERVIEW.HAS_PROGRESS_CURRENT,
                    overview.getProgressCurrent());
        }

        if (overview.getProgressTotal() != null) {
            statements.addInt(
                    iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL,
                    overview.getProgressTotal());
        }

        if (overview.getProgressTotalMap() != null) {
            statements.addInt(
                    iri, LP_OVERVIEW.HAS_PROGRESS_TOTAL_MAP,
                    overview.getProgressTotalMap());
        }

        if (overview.getProgressCurrentExecuted() != null) {
            statements.addInt(
                    iri, LP_OVERVIEW.HAS_PROGRESS_EXECUTED,
                    overview.getProgressCurrentExecuted());
        }

        if (overview.getProgressCurrentMapped() != null) {
            statements.addInt(
                    iri, LP_OVERVIEW.HAS_PROGRESS_MAPPED,
                    overview.getProgressCurrentMapped());
        }

        return statements;
    }

    private Statements deletedAsStatements(Execution execution) {
        IRI iri = this.valueFactory.createIRI(execution.getIri());
        Statements statements = Statements.arrayList();
        statements.setDefaultGraph(execution.getListGraph());
        statements.addIri(iri, RDF.TYPE, LP_LIST.TOMBSTONE);
        return statements;
    }

}
