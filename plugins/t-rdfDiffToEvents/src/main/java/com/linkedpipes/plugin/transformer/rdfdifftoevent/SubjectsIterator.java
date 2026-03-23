package com.linkedpipes.plugin.transformer.rdfdifftoevent;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Iterates over distinct IRI subjects in a named graph, in ascending
 * alphabetical order, using paginated SPARQL queries.
 */
class SubjectsIterator implements Iterator<IRI> {

    private static final int PAGE_SIZE = 250;

    private final RepositoryConnection connection;
    private final String graphIri;
    private final Deque<IRI> queue = new ArrayDeque<>();
    private IRI next;
    private int offset = 0;

    SubjectsIterator(RepositoryConnection connection, Resource readGraph) {
        this.connection = connection;
        this.graphIri = readGraph.stringValue();
    }

    @Override
    public boolean hasNext() {
        if (next == null) {
            next = fetchNext();
        }
        return next != null;
    }

    @Override
    public IRI next() {
        IRI result = next;
        next = null;
        offset++;
        return result;
    }

    private IRI fetchNext() {
        // Return from queue if items are buffered from previous page fetch.
        IRI queued = queue.pollFirst();
        if (queued != null) {
            return queued;
        }

        // Queue exhausted — fetch the next page.
        String query =
                "SELECT DISTINCT ?s " +
                "WHERE { GRAPH <" + graphIri + "> { ?s ?p ?o . } " +
                "FILTER(isIRI(?s)) } " +
                "ORDER BY ?s " +
                "LIMIT " + PAGE_SIZE + " OFFSET " + offset;

        try (TupleQueryResult result =
                     connection.prepareTupleQuery(query).evaluate()) {
            for (BindingSet binding : result) {
                queue.add((IRI) binding.getValue("s"));
            }
        }

        return queue.pollFirst();
    }
}
