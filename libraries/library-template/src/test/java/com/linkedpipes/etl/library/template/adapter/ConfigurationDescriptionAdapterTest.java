package com.linkedpipes.etl.library.template.adapter;

import com.linkedpipes.etl.library.template.configuration.adapter.ConfigurationDescriptionToRdf;
import com.linkedpipes.etl.library.template.configuration.adapter.RdfToConfigurationDescription;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationDescriptionAdapterTest {

    public ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void toRdfAndBack() {
        Map<IRI, ConfigurationDescription.Member> members = new HashMap<>();
        members.put(valueFactory.createIRI("http://name"),
                new ConfigurationDescription.Member(
                        valueFactory.createIRI("http://name"),
                        valueFactory.createIRI("http://nameControl"),
                        valueFactory.createIRI("http://nameSubstitution"),
                        false));
        members.put(valueFactory.createIRI("http://passwd"),
                new ConfigurationDescription.Member(
                        valueFactory.createIRI("http://passwd"),
                        valueFactory.createIRI("http://passwdControl"),
                        valueFactory.createIRI("http://passwdSubstitution"),
                        true));

        var expected = new ConfigurationDescription(
                valueFactory.createIRI("http://description"),
                valueFactory.createIRI("http://resource/Configuration"),
                valueFactory.createIRI("http://gloabl"),
                members);
        var graph = valueFactory.createIRI("http://graph");
        var statements = ConfigurationDescriptionToRdf.asRdf(expected, graph);
        List<ConfigurationDescription> candidates =
                RdfToConfigurationDescription.asConfigurationDescriptions(
                        statements.selector());
        Assertions.assertEquals(1, candidates.size());
        var actual  = candidates.get(0);
        Assertions.assertEquals(expected, actual);
    }

}
