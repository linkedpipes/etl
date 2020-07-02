package com.linkedpipes.etl.plugin.configuration;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class SelectPrivateStatements {

    public List<Statement> selectPrivate(
            List<Statement> rdf, Description description) {
        Set<IRI> privatePredicates = description.getMembers().stream()
                .filter(Description.Member::isPrivate)
                .map(Description.Member::getProperty)
                .collect(Collectors.toSet());
        if (privatePredicates.isEmpty()) {
            return Collections.emptyList();
        }
        return rdf.stream().filter(
                (st) -> privatePredicates.contains(st.getPredicate()))
                .collect(Collectors.toList());
    }

}
