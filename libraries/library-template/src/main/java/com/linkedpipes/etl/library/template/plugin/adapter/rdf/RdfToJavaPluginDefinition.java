package com.linkedpipes.etl.library.template.plugin.adapter.rdf;

import com.linkedpipes.etl.library.template.plugin.model.JavaPluginDefinition;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RdfToJavaPluginDefinition {

    private static final String JAR_FILE =
            "http://etl.linkedpipes.com/ontology/JarFile";

    private static final String HAS_DIRECTORY =
            "http://etl.linkedpipes.com/ontology/directory";

    public static List<JavaPluginDefinition> asJarPluginDefinitions(
            StatementsSelector statements) {
        Collection<Resource> resources =
                statements.selectByType(JAR_FILE)
                        .subjects();
        List<JavaPluginDefinition> result = new ArrayList<>();
        for (Resource resource : resources) {
            if (!resource.isIRI()) {
                continue;
            }
            result.add(loadJarPluginDefinitions(statements, (IRI) resource));
        }
        return result;
    }

    private static JavaPluginDefinition loadJarPluginDefinitions(
            StatementsSelector statements, IRI resource) {
        List<String> directories = new ArrayList<>();
        for (Statement statement : statements.withSubject(resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case HAS_DIRECTORY:
                    if (value.isLiteral()) {
                        directories.add(value.stringValue());
                    }
                    break;
            }
        }
        return new JavaPluginDefinition(resource, directories);
    }

}
