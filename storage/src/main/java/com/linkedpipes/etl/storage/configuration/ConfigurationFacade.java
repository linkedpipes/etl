package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.rdf.RdfObjects;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.Collection;

/**
 * @author Petr Škoda
 */
public class ConfigurationFacade {

    private ConfigurationFacade() {

    }

    /**
     * @param configurationRdf Configuration.
     * @param descriptionRdf Description of the configuration.
     * @param baseIri Resource used for generated configuration.
     * @param graph Graph used for generated configuration.
     * @return
     */
    public static Collection<Statement> createConfigurationTemplate(
            Collection<Statement> configurationRdf,
            Collection<Statement> descriptionRdf,
            String baseIri, IRI graph) throws BaseException {
        // Load description.
        final ConfigDescription description = new ConfigDescription();
        PojoLoader.loadOfType(descriptionRdf, ConfigDescription.TYPE,
                description);
        //
        if (description.getType() == null) {
            throw new BaseException("Missing configuration type.");
        }
        // Load configuration.
        final RdfObjects config = new RdfObjects(configurationRdf);
        // Update configuration.
        final ValueFactory vf = SimpleValueFactory.getInstance();
        config.getTyped(description.getType()).forEach((instance) -> {
            for (ConfigDescription.Member member : description.getMembers()) {
                switch (instance.getReference(member.getControl()).getResource()
                        .stringValue()) {
                    case "http://plugins.linkedpipes.com/resource/configuration/None":
                    case "http://plugins.linkedpipes.com/resource/configuration/Inherit":
                        // Do nothing.
                        break;
                    case "http://plugins.linkedpipes.com/resource/configuration/Force":
                    case "http://plugins.linkedpipes.com/resource/configuration/InheritAndForce":
                        // Values if forced by parent - so we need to removeProperties it.
                        instance.delete(member.getControl());
                        instance.delete(member.getProperty());
                        instance.add(member.getControl(), vf.createIRI(
                                "http://plugins.linkedpipes.com/resource/configuration/Forced"));
                        break;
                    case "http://plugins.linkedpipes.com/resource/configuration/Forced":
                        // The value is forced by parent, do nothing with it.
                        break;
                }
            }
        });
        // Update resources and return.
        config.updateTypedResources(baseIri);
        return config.asStatements(graph);
    }

}
