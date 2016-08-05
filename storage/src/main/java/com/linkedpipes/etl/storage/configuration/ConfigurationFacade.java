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
     * @param descriptionRdf   Description of the configuration.
     * @param baseIri          Resource used for generated configuration.
     * @param graph            Graph used for generated configuration.
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
                switch (instance.getReference(member.getControl()).getResource().stringValue()) {
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

//    public static Collection<Statement> createConfigurationTemplate(
//            Collection<Statement> configurationRdf,
//            Collection<Statement> descriptionRdf,
//            String baseIri, IRI graph) throws BaseException {
//        // Load description.
//        final ConfigDescription description = new ConfigDescription();
//        PojoLoader.loadOfType(descriptionRdf, ConfigDescription.TYPE,
//                description);
//        //
//        if (description.getType() == null) {
//            throw new BaseException("Missing configuration type.");
//        }
//        // Load configuration.
//        final Resource configResource = RdfUtils.find(configurationRdf,
//                description.getType());
//        final RdfObject config = RdfObject.create(configurationRdf,
//                configResource);
//        // Update configuration.
//        final ValueFactory vf = SimpleValueFactory.getInstance();
//        for (ConfigDescription.Member member : description.getMembers()) {
//            switch (config.getProperty(member.getControl()).stringValue()) {
//                case "http://plugins.linkedpipes.com/resource/configuration/None":
//                case "http://plugins.linkedpipes.com/resource/configuration/Inherit":
//                    // Do nothing.
//                    break;
//                case "http://plugins.linkedpipes.com/resource/configuration/Force":
//                case "http://plugins.linkedpipes.com/resource/configuration/InheritAndForce":
//                    // Values if forced by parent - so we need to removeProperties it.
//                    RdfObject.removeProperties(config, member.getControl());
//                    RdfObject.removeProperties(config, member.getProperty());
//                    RdfObject.add(config, member.getControl(), vf.createIRI(
//                            "http://plugins.linkedpipes.com/resource/configuration/Forced"));
//                    break;
//                case "http://plugins.linkedpipes.com/resource/configuration/Forced":
//                    // The value is forced by parent, do nothing with it.
//                    break;
//            }
//        }
//        // Update resources and return.
//        RdfObject.updateTypedResources(config, baseIri);
//        return RdfObject.collect(
//                config, graph, SimpleValueFactory.getInstance());
//    }

//    public static void addConfigurationObject(StatementsCollection pipeline,
//            Resource componentResource, IRI graph) {
//        // Find references to configuration graphs.
//        StatementsCollection componentConfig = pipeline.filter((s) -> {
//            return s.getSubject().equals(componentResource) &&
//                    s.getPredicate().stringValue().equals(
//                            "http://linkedpipes.com/ontology/configurationGraph"
//                    );
//        });
//        if (componentConfig.size() == 0) {
//            // There is no configuration.
//            return;
//        } if (componentConfig.size() != 1) {
//            throw new RuntimeException("Invalid number of configurations for: "
//                + componentResource.stringValue());
//        }
//        pipeline.remove(componentConfig);
//        // Get number of existing references.
//        Integer configSize = pipeline.filter((s) -> {
//            return s.getSubject().equals(componentResource) &&
//                    s.getPredicate().stringValue().equals(
//                            "http://linkedpipes.com/ontology/configuration"
//                    );
//        }).size();
//
//        final ValueFactory vf = SimpleValueFactory.getInstance();
//        final IRI configIri = vf.createIRI(componentResource +
//                "/configuration/" + configSize);
//
//        final Collection<Statement> toAdd = Arrays.asList(
//                vf.createStatement(componentResource,
//                        vf.createIRI(
//                                "http://linkedpipes.com/ontology/configuration"),
//                        configIri, graph),
//                vf.createStatement(configIri, RDF.TYPE,
//                        vf.createIRI(
//                                "http://linkedpipes.com/ontology/Configuration"),
//                                graph),
//                vf.createStatement(configIri,
//                        vf.createIRI(
//                                "http://linkedpipes.com/ontology/configuration/graph"),
//                        componentConfig.getStatements().iterator().next().getObject(),
//                        graph),
//                vf.createStatement(configIri,
//                        vf.createIRI(
//                                "http://http://linkedpipes.com/ontology/configuration/order"),
//                        vf.createLiteral(configSize + 1),
//                        graph)
//        );
//        // a http://linkedpipes.com/ontology/Configuration
//        // http://linkedpipes.com/ontology/configuration/graph {}
//        // http://linkedpipes.com/ontology/configuration/order
//        pipeline.addAll(toAdd);
//        return;
//    }

}
