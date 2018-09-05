package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_LIST;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import com.linkedpipes.etl.rdf4j.Statements;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class OverviewToStatements {

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
        Statements statements = Statements.ArrayList();
        statements.setDefaultGraph(execution.getListGraph());

        statements.addIri(iri, RDF.TYPE, LP_EXEC.EXECUTION);
        statements.addIri(iri, LP_OVERVIEW.HAS_STATUS, overview.getStatus());
        statements.add(iri, LP_EXEC.HAS_SIZE, this.getDirSizeValue(execution));

        if (overview.getPipeline() != null) {
            statements.addIri(
                    iri, LP_OVERVIEW.HAS_PIPELINE,
                    overview.getPipeline());
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

        return statements;
    }

    private Value getDirSizeValue(Execution execution) {
        return this.valueFactory.createLiteral(
                FileUtils.sizeOfDirectory(execution.getDirectory()));
    }

    private Statements deletedAsStatements(Execution execution) {
        IRI iri = this.valueFactory.createIRI(execution.getIri());
        Statements statements = Statements.ArrayList();
        statements.setDefaultGraph(execution.getListGraph());
        statements.addIri(iri, RDF.TYPE, LP_LIST.TOMBSTONE);
        return statements;
    }

}
