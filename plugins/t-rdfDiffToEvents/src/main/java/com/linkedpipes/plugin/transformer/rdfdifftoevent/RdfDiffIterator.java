package com.linkedpipes.plugin.transformer.rdfdifftoevent;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Merge-sort style iterator that compares two named graphs by subject IRI
 * and emits a {@link CrudEvent} for each subject describing whether it was
 * created, updated, deleted, or unchanged.
 *
 * <p>The Concise Bounded Description (CBD) of each subject is collected,
 * including recursively-nested blank nodes.
 */
class RdfDiffIterator implements Iterator<CrudEvent> {

    private final RepositoryConnection leftConn;
    private final Resource leftGraph;
    private final RepositoryConnection rightConn;
    private final Resource rightGraph;

    private final SubjectsIterator leftSubjects;
    private final SubjectsIterator rightSubjects;

    private IRI leftCursor;
    private IRI rightCursor;

    RdfDiffIterator(
            RepositoryConnection leftConn, Resource leftGraph,
            RepositoryConnection rightConn, Resource rightGraph) {
        this.leftConn = leftConn;
        this.leftGraph = leftGraph;
        this.rightConn = rightConn;
        this.rightGraph = rightGraph;

        this.leftSubjects = new SubjectsIterator(leftConn, leftGraph);
        this.rightSubjects = new SubjectsIterator(rightConn, rightGraph);

        leftCursor = leftSubjects.hasNext() ? leftSubjects.next() : null;
        rightCursor = rightSubjects.hasNext() ? rightSubjects.next() : null;
    }

    @Override
    public boolean hasNext() {
        return leftCursor != null || rightCursor != null;
    }

    @Override
    public CrudEvent next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // Subject present only in the right graph → CREATE.
        if (leftCursor == null) {
            CrudEvent event = new CrudEvent(
                    CrudType.CREATE, rightCursor,
                    getCBD(rightConn, rightCursor, rightGraph));
            rightCursor = rightSubjects.hasNext() ? rightSubjects.next() : null;
            return event;
        }

        // Subject present only in the left graph → DELETE.
        if (rightCursor == null) {
            CrudEvent event = new CrudEvent(CrudType.DELETE, leftCursor);
            leftCursor = leftSubjects.hasNext() ? leftSubjects.next() : null;
            return event;
        }

        int cmp = leftCursor.stringValue().compareTo(rightCursor.stringValue());

        if (cmp == 0) {
            // Same subject in both graphs — compare CBDs.
            Model leftCBD = getCBD(leftConn, leftCursor, leftGraph);
            Model rightCBD = getCBD(rightConn, rightCursor, rightGraph);
            CrudEvent event;
            if (Models.isomorphic(leftCBD, rightCBD)) {
                event = new CrudEvent(CrudType.NOOP, leftCursor);
            } else {
                event = new CrudEvent(CrudType.UPDATE, leftCursor, rightCBD);
            }
            leftCursor = leftSubjects.hasNext() ? leftSubjects.next() : null;
            rightCursor = rightSubjects.hasNext() ? rightSubjects.next() : null;
            return event;
        }

        if (cmp < 0) {
            // leftCursor sorts before rightCursor → subject only in left → DELETE.
            CrudEvent event = new CrudEvent(CrudType.DELETE, leftCursor);
            leftCursor = leftSubjects.hasNext() ? leftSubjects.next() : null;
            return event;
        }

        // rightCursor sorts before leftCursor → subject only in right → CREATE.
        CrudEvent event = new CrudEvent(
                CrudType.CREATE, rightCursor,
                getCBD(rightConn, rightCursor, rightGraph));
        rightCursor = rightSubjects.hasNext() ? rightSubjects.next() : null;
        return event;
    }

    /**
     * Returns the Concise Bounded Description of {@code rootSubject} from
     * the given named graph, following blank-node chains recursively.
     */
    private Model getCBD(
            RepositoryConnection conn, IRI rootSubject, Resource graph) {
        Stack<Resource> stack = new Stack<>();
        LinkedHashModel model = new LinkedHashModel();
        List<Resource> processed = new ArrayList<>();

        stack.push(rootSubject);
        while (!stack.isEmpty()) {
            Resource subject = stack.pop();
            processed.add(subject);
            try (RepositoryResult<Statement> stmts =
                         conn.getStatements(subject, null, null, graph)) {
                for (Statement stmt : stmts) {
                    model.add(
                            stmt.getSubject(),
                            stmt.getPredicate(),
                            stmt.getObject());
                    if (stmt.getObject().isBNode()) {
                        Resource bnode = (Resource) stmt.getObject();
                        if (!processed.contains(bnode)) {
                            stack.push(bnode);
                        }
                    }
                }
            }
        }
        return model;
    }
}
