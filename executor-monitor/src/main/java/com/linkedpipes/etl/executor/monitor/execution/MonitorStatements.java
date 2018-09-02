package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_LIST;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

class MonitorStatements {

    private static final String HAS_MONITOR_STATUS =
            "http://etl.linkedpipes.com/ontology/statusMonitor";

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public void update(Execution execution) {
        Statements statements = Statements.ArrayList();
        statements.setDefaultGraph(execution.getListGraph());
        IRI iri = this.valueFactory.createIRI(execution.getIri());
        if (execution.getStatus() == ExecutionStatus.DELETED) {
            addTombstoneStatements(statements, iri);
        } else {
            statements.add(
                    iri, HAS_MONITOR_STATUS, execution.getStatus().getIri());
        }
        execution.setMonitorStatements(statements);
    }

    private void addTombstoneStatements(Statements statements, IRI iri) {
        statements.addIri(iri, RDF.TYPE, LP_LIST.TOMBSTONE);
    }

}
