package com.linkedpipes.plugin.check.rdfsinglegraph;

import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;

class RemoveGraphIterator implements Iteration<Statement, RepositoryException> {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final RepositoryResult<Statement> source;

    public RemoveGraphIterator(RepositoryResult<Statement> source) {
        this.source = source;
    }

    @Override
    public boolean hasNext() throws RepositoryException {
        return source.hasNext();
    }

    @Override
    public Statement next() throws RepositoryException {
        Statement st = source.next();
        if (st == null) {
            return null;
        } else {
            return valueFactory.createStatement(
                    st.getSubject(), st.getPredicate(), st.getObject());
        }
    }

    @Override
    public void remove() throws RepositoryException {
        source.remove();
    }

}
