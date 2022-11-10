package com.linkedpipes.etl.library.template.configuration;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Change configuration resources.
 */
class LocalizeConfiguration {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public Statements localizeConfiguration(
            ConfigurationDescription description,
            StatementsSelector statements, Resource resource) {
        Collection<Resource> resources =
                statements.selectByType(description.configurationType())
                        .subjects();
        Map<Resource, Resource> map = createMap(resources, resource);
        Statements result = Statements.arrayList(statements.size());
        for (Statement st : statements) {
            result.add(valueFactory.createStatement(
                    map.getOrDefault(st.getSubject(), st.getSubject()),
                    st.getPredicate(),
                    st.getObject()
            ));
        }
        return result;
    }

    private Map<Resource, Resource> createMap(
            Collection<Resource> resources, Resource base) {
        Map<Resource, Resource> result = new HashMap<>();
        int counter = 0;
        for (Resource resource : resources) {
            Resource next = valueFactory.createIRI(
                    base.stringValue() + "/" + ++counter);
            result.put(resource, next);
        }
        return result;
    }

}
