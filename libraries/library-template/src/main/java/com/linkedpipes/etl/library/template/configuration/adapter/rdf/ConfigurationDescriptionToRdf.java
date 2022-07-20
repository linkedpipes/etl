package com.linkedpipes.etl.library.template.configuration.adapter.rdf;

import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.library.template.vocabulary.LP_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class ConfigurationDescriptionToRdf {

    public static Statements asRdf(
            ConfigurationDescription description,
            Resource graph) {
        StatementsBuilder result = Statements.arrayList().builder();
        result.setDefaultGraph(graph);
        Resource resource = description.resource();
        result.addIri(resource,
                RDF.TYPE, LP_V1.CONFIG_DESCRIPTION);
        result.add(resource,
                LP_V1.CONFIG_DESC_TYPE, description.configurationType());
        result.add(resource,
                LP_V1.CONFIG_DESC_CONTROL, description.globalControlProperty());
        int counter = 0;
        for (var entry : description.members().entrySet()) {
            Resource memberResource =
                    createMemberResource(resource, counter++);
            result.add(resource, LP_V1.HAS_MEMBER, memberResource);
            writeMember(
                    memberResource, entry.getKey(), entry.getValue(),
                    result);

        }
        return result;
    }

    private static IRI createMemberResource(Resource resource, int counter) {
        return SimpleValueFactory.getInstance().createIRI(
                resource.stringValue() + "/" + String.format("%03d", counter));
    }

    private static void writeMember(
            Resource resource, IRI property,
            ConfigurationDescription.Member member,
            StatementsBuilder builder) {
        builder.addIri(resource, RDF.TYPE, LP_V1.MEMBER);
        builder.add(resource, LP_V1.CONFIG_DESC_PROPERTY, property);
        builder.add(resource, LP_V1.CONFIG_DESC_CONTROL, member.control());
        builder.add(resource, LP_V1.IS_PRIVATE, member.isPrivate());
    }

}
