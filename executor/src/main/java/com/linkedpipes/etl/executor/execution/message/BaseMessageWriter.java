package com.linkedpipes.etl.executor.execution.message;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EVENTS;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.rdf.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

class BaseMessageWriter {

    private static final int DATE_STRING_LEN = 25;

    private final DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");

    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    protected final IRI executionIri;

    protected final ValueFactory valueFactory =
            SimpleValueFactory.getInstance();

    protected final StatementsBuilder statements =
            Statements.arrayList().builder();

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
        String iriAsString = executionIri.stringValue() + "/messages/"
                + Integer.toString(index);
        return this.valueFactory.createIRI(iriAsString);
    }

    protected void createBaseEvent(IRI iri, int order) {
        statements.add(this.executionIri, LP_EVENTS.HAS_EVENT, iri);
        statements.addType(iri, LP_EVENTS.EVENT);
        statements.add(iri, LP_EVENTS.HAS_CREATED, getNowDate());
        statements.add(iri, LP_EVENTS.HAS_ORDER,
                this.valueFactory.createLiteral(order));
    }

    private Value getNowDate() {
        Date now = new Date();
        StringBuilder createdAsString = new StringBuilder(DATE_STRING_LEN);
        createdAsString.append(dateFormat.format(now));
        createdAsString.append("T");
        createdAsString.append(timeFormat.format(now));
        return this.valueFactory.createLiteral(
                createdAsString.toString(),
                this.valueFactory.createIRI(XSD.DATETIME));
    }

    public void save() throws IOException {
        Rdf4jUtils.save(this.statements, this.file);
    }

}
