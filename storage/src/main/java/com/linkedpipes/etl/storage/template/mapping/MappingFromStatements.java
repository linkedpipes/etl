package com.linkedpipes.etl.storage.template.mapping;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.OWL;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class MappingFromStatements {

    private final MappingFacade mappingFacade;

    public MappingFromStatements(MappingFacade mappingFacade) {
        this.mappingFacade = mappingFacade;
    }

    public Mapping create(Collection<Statement> statements) {
        Map<String, String> remoteToOriginal = new HashMap<>();
        Map<String, String> remoteToLocal = new HashMap<>();

        statements.stream()
                .filter((s) -> s.getContext().equals(MappingFacade.GRAPH))
                .filter((s) -> s.getPredicate().equals(OWL.SAMEAS))
                .forEach((s) -> {
                    String original = s.getSubject().stringValue();
                    String remote = s.getObject().stringValue();
                    remoteToOriginal.put(remote, original);
                    // Try to resolve.
                    String local = mappingFacade.originalToLocal(original);
                    if (local != null) {
                        remoteToLocal.put(remote, local);
                    }
                });

        return new Mapping(remoteToOriginal, remoteToLocal, mappingFacade);
    }

}
