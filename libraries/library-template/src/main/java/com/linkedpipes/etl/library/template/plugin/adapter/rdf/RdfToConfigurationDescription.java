package com.linkedpipes.etl.library.template.plugin.adapter.rdf;

import com.linkedpipes.etl.library.template.plugin.model.ConfigurationDescription;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RdfToConfigurationDescription {

    /**
     * This function does not load the resources.
     */
    public static List<ConfigurationDescription> asConfigurationDescriptions(
            StatementsSelector statements) {
        Collection<Resource> candidates = statements
                .selectByType(LP_V1.CONFIG_DESCRIPTION)
                .subjects();
        List<ConfigurationDescription> result =
                new ArrayList<>(candidates.size());
        for (Resource resource : candidates) {
            IRI configurationType = null, globalControlProperty = null;
            Map<IRI, ConfigurationDescription.Member> members = new HashMap<>();
            for (Statement statement : statements.withSubject(resource)) {
                Value value = statement.getObject();
                switch (statement.getPredicate().stringValue()) {
                    case LP_V1.CONFIG_DESC_TYPE:
                        if (value.isIRI()) {
                            configurationType = (IRI) value;
                        }
                        break;
                    case LP_V1.CONFIG_DESC_MEMBER:
                        if (value.isResource()) {
                            loadMember((Resource) value, statements, members);
                        }
                        break;
                    case LP_V1.CONFIG_DESC_CONTROL:
                        if (value.isIRI()) {
                            globalControlProperty = (IRI) value;
                        }
                        break;
                    default:
                        break;
                }
            }
            result.add(new ConfigurationDescription(
                    resource, configurationType, globalControlProperty,
                    members));
        }
        return result;
    }

    private static void loadMember(
            Resource resource, StatementsSelector statements,
            Map<IRI, ConfigurationDescription.Member> collector) {
        IRI property = null, controlProperty = null;
        boolean isPrivate = false;
        for (Statement statement : statements.withSubject(resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.CONFIG_DESC_PROPERTY:
                    if (value.isIRI()) {
                        property = (IRI) value;
                    }
                    break;
                case LP_V1.CONFIG_DESC_CONTROL:
                    if (value.isIRI()) {
                        controlProperty = (IRI) value;
                    }
                    break;
                case LP_V1.IS_PRIVATE:
                    if (value.isLiteral()) {
                        isPrivate = ((Literal) value).booleanValue();
                    }
                    break;
                default:
                    break;
            }
        }
        ConfigurationDescription.Member member =
                new ConfigurationDescription.Member(
                        controlProperty, isPrivate);
        collector.put(property, member);
    }

}
