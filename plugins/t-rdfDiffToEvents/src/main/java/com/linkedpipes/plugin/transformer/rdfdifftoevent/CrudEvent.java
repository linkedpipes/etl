package com.linkedpipes.plugin.transformer.rdfdifftoevent;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

enum CrudType {
    CREATE, UPDATE, NOOP, DELETE
}

class CrudEvent {

    private final CrudType type;
    private final IRI subject;
    private final Model graph;

    CrudEvent(CrudType type, IRI subject, Model graph) {
        this.type = type;
        this.subject = subject;
        this.graph = graph;
    }

    CrudEvent(CrudType type, IRI subject) {
        this(type, subject, new LinkedHashModel());
    }

    CrudType getType() {
        return type;
    }

    IRI getSubject() {
        return subject;
    }

    Model getGraph() {
        return graph;
    }

    boolean isNoOp() {
        return type == CrudType.NOOP;
    }
}
