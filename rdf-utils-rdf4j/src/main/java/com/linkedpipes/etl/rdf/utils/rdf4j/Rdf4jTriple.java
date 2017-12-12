package com.linkedpipes.etl.rdf.utils.rdf4j;

import com.linkedpipes.etl.rdf.utils.model.RdfTriple;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import org.eclipse.rdf4j.model.Statement;

class Rdf4jTriple implements RdfTriple {

    private final Statement statement;

    public Rdf4jTriple(Statement statement) {
        this.statement = statement;
    }

    @Override
    public String getSubject() {
        return statement.getSubject().stringValue();
    }

    @Override
    public String getPredicate() {
        return statement.getPredicate().stringValue();
    }

    @Override
    public BackendRdfValue getObject() {
        return new Rdf4jValue(statement.getObject());
    }

}
