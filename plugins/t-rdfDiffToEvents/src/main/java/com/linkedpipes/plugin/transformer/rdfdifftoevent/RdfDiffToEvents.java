package com.linkedpipes.plugin.transformer.rdfdifftoevent;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * Compares two single-graph RDF data units (left = old, right = new) and
 * writes CRUD diff events to the output graph.
 *
 * <p>Each subject IRI that differs between the two graphs receives a
 * {@code lpdiff:diffEventType} triple whose object is one of
 * {@code lpdiff:Create}, {@code lpdiff:Update}, or {@code lpdiff:Delete}.
 * For Create and Update events the full Concise Bounded Description (CBD)
 * of the subject from the right graph is also written to the output.
 * Unchanged subjects (NOOP) produce no output.
 */
public class RdfDiffToEvents implements Component, SequentialExecution {

    static final String DIFF_NS =
            "http://etl.linkedpipes.com/ontology/rdf-diff#";
    static final String DIFF_EVENT_TYPE = DIFF_NS + "diffEventType";
    static final String CREATE_TYPE    = DIFF_NS + "Create";
    static final String UPDATE_TYPE    = DIFF_NS + "Update";
    static final String DELETE_TYPE    = DIFF_NS + "Delete";

    @Component.InputPort(iri = "LeftRdf")
    public SingleGraphDataUnit leftRdf;

    @Component.InputPort(iri = "RightRdf")
    public SingleGraphDataUnit rightRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Override
    public void execute() throws LpException {
        leftRdf.execute((leftConn) -> {
            rightRdf.execute((rightConn) -> {
                outputRdf.execute((outConn) -> {
                    Resource outGraph = outputRdf.getWriteGraph();
                    ValueFactory vf = outConn.getValueFactory();

                    IRI eventTypePred = vf.createIRI(DIFF_EVENT_TYPE);
                    IRI createIri    = vf.createIRI(CREATE_TYPE);
                    IRI updateIri    = vf.createIRI(UPDATE_TYPE);
                    IRI deleteIri    = vf.createIRI(DELETE_TYPE);

                    RdfDiffIterator iter = new RdfDiffIterator(
                            leftConn,  leftRdf.getReadGraph(),
                            rightConn, rightRdf.getReadGraph());

                    while (iter.hasNext()) {
                        CrudEvent event = iter.next();
                        if (event.isNoOp()) {
                            continue;
                        }

                        IRI subject = event.getSubject();
                        IRI typeIri = switch (event.getType()) {
                            case CREATE -> createIri;
                            case UPDATE -> updateIri;
                            case DELETE -> deleteIri;
                            case NOOP   -> throw new IllegalStateException();
                        };

                        // Write the event-type marker on the subject.
                        outConn.add(subject, eventTypePred, typeIri, outGraph);

                        // For CREATE/UPDATE include the right-graph CBD.
                        if (!event.getGraph().isEmpty()) {
                            outConn.add(event.getGraph(), outGraph);
                        }
                    }
                });
            });
        });
    }
}
