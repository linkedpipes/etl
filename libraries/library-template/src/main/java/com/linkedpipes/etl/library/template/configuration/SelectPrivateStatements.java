package com.linkedpipes.etl.library.template.configuration;

import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.template.plugin.model.ConfigurationDescription;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class SelectPrivateStatements {

    public List<Statement> selectPrivate(
            ConfigurationDescription description,
            StatementsSelector statements) {
        Set<IRI> privatePredicates = description.members().entrySet()
                .stream().filter(entry -> entry.getValue().isPrivate())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (privatePredicates.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Statement> result = new ArrayList<>();
        Collection<Resource> subjects =
                statements.selectByType(
                        description.configurationType()).subjects();
        for (Resource subject : subjects) {
            for (Statement statement : statements.withSubject(subject)) {
                if (!privatePredicates.contains(statement.getPredicate())) {
                    continue;
                }
                result.add(statement);
            }
        }
        return result;
    }

}
