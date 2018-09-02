package com.linkedpipes.etl.executor.execution.message;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EVENTS;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

class BaseMessageWriter {

    private final static DateFormat DATE_FORMAT
            = new SimpleDateFormat("YYYY-MM-dd");

    private final static DateFormat TIME_FORMAT
            = new SimpleDateFormat("HH:mm:ss.SSS");

    protected final IRI executionIri;

    protected final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    protected final Statements statements = new Statements(new ArrayList<>());

    protected final AtomicInteger messageCounter;

    private final File file;

    public BaseMessageWriter(
            String executionIri, AtomicInteger eventCounter, File file) {
        this.executionIri = valueFactory.createIRI(executionIri);
        this.messageCounter = eventCounter;
        this.file = file;
        this.statements.setDefaultGraph(this.executionIri);
    }

    public Statements getStatements() {
        return this.statements;
    }

    protected IRI createEventIri(int index) {
        String iriAsString = executionIri.stringValue() + "/messages/" +
                Integer.toString(index);
        return this.valueFactory.createIRI(iriAsString);
    }

    protected void createBaseEvent(IRI iri, int order) {
        statements.add(this.executionIri, LP_EVENTS.HAS_EVENT, iri);
        statements.addIri(iri, RDF.TYPE, LP_EVENTS.EVENT);
        statements.add(iri, LP_EVENTS.HAS_CREATED, getNowDate());
        statements.add(iri, LP_EVENTS.HAS_ORDER,
                this.valueFactory.createLiteral(order));
    }

    private Value getNowDate() {
        Date now = new Date();
        StringBuilder createdAsString = new StringBuilder(25);
        createdAsString.append(DATE_FORMAT.format(now));
        createdAsString.append("T");
        createdAsString.append(TIME_FORMAT.format(now));
        return this.valueFactory.createLiteral(
                createdAsString.toString(),
                this.valueFactory.createIRI(XSD.DATETIME));
    }

    public void save() throws IOException {
        Rdf4jUtils.save(this.statements, this.file);
    }

}
