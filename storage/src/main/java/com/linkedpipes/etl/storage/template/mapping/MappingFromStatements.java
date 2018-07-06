package com.linkedpipes.etl.storage.template.mapping;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.OWL;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class MappingFromStatements {

    private final Map<String, String> originalToLocal;

    private final IRI graph;

    public MappingFromStatements(Map<String, String> mappings, IRI graph) {
        this.originalToLocal = mappings;
        this.graph = graph;
    }

    public Mapping create(Collection<Statement> statements) {
        Map<String, String> remoteToOriginal = new HashMap<>();
        Map<String, String> remoteToLocal = new HashMap<>();
        statements.stream()
                .filter((s) -> s.getContext().equals(this.graph))
                .filter((s) -> s.getPredicate().equals(OWL.SAMEAS))
                .forEach((s) -> {
                    String original = s.getSubject().stringValue();
                    String remote = s.getObject().stringValue();
                    remoteToOriginal.put(remote, original);
                    String local = originalToLocal.get(original);
                    if (local != null) {
                        remoteToLocal.put(remote, local);
                    }
                });
        return new Mapping(remoteToOriginal, remoteToLocal, originalToLocal);
    }

}
