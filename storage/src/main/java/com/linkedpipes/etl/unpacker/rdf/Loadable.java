package com.linkedpipes.etl.unpacker.rdf;

import com.linkedpipes.etl.library.rdf.StatementsSelector;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public interface Loadable {

    default void resource(String resource) {
        // No operation here.
    }

    Loadable load(String predicate, Value value);

    static void load(
            StatementsSelector statements,
            Loadable target, Resource resource) {
        Map<Loadable, Resource> entities = new HashMap<>();
        Stack<Loadable> queue = new Stack<>();
        entities.put(target, resource);
        queue.push(target);
        while (!queue.isEmpty()) {
            Loadable next = queue.pop();
            Resource subject = entities.get(next);
            next.resource(subject.stringValue());
            for (Statement statement : statements.withSubject(subject)) {
                Value value = statement.getObject();
                Loadable newEntity = next.load(
                        statement.getPredicate().stringValue(),
                        value);
                if (newEntity == null) {
                    continue;
                } else if (entities.containsKey(newEntity)) {
                    continue;
                } else if (value instanceof Resource newSubject) {
                    queue.add(newEntity);
                    entities.put(newEntity, newSubject);
                }
            }
        }
    }

}
