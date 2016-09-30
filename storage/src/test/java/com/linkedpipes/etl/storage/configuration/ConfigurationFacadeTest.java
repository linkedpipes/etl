package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.storage.BaseException;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Petr Škoda
 */
public class ConfigurationFacadeTest {

    private final ValueFactory VF = SimpleValueFactory.getInstance();

    @Test
    public void simpleConfigurationCreate() throws BaseException {
        final Resource configRes = VF.createBNode();
        final List<Statement> configRdf = Arrays.asList(
                VF.createStatement(configRes, RDF.TYPE,
                        VF.createIRI("http://localhost/ontology/TestConfig")),
                VF.createStatement(configRes,
                        VF.createIRI("http://localhost/ontology/value/1"),
                        VF.createLiteral("Value-1")),
                VF.createStatement(configRes,
                        VF.createIRI("http://localhost/ontology/control/1"),
                        VF.createIRI(
                                "http://plugins.linkedpipes.com/resource/configuration/Inherit")),
                VF.createStatement(configRes,
                        VF.createIRI("http://localhost/ontology/value/2"),
                        VF.createLiteral("Value-2")),
                VF.createStatement(configRes,
                        VF.createIRI("http://localhost/ontology/control/2"),
                        VF.createIRI(
                                "http://plugins.linkedpipes.com/resource/configuration/Force"))
        );
        final Resource descRes1 = VF.createBNode();
        final Resource descRes2 = VF.createBNode();
        final Resource descRes3 = VF.createBNode();
        final List<Statement> descRdf = Arrays.asList(
                VF.createStatement(descRes1, RDF.TYPE, ConfigDescription.TYPE),
                VF.createStatement(descRes1,
                        VF.createIRI(
                                "http://plugins.linkedpipes.com/ontology/configuration/type"),
                        VF.createIRI("http://localhost/ontology/TestConfig")),
                VF.createStatement(descRes1,
                        VF.createIRI(
                                "http://plugins.linkedpipes.com/ontology/configuration/member"),
                        descRes2),
                VF.createStatement(descRes1,
                        VF.createIRI(
                                "http://plugins.linkedpipes.com/ontology/configuration/member"),
                        descRes3),
                //
                VF.createStatement(descRes2,
                        VF.createIRI(
                                "http://plugins.linkedpipes.com/ontology/configuration/property"),
                        VF.createIRI("http://localhost/ontology/value/1")),
                VF.createStatement(descRes2,
                        VF.createIRI(
                                "http://plugins.linkedpipes.com/ontology/configuration/control"),
                        VF.createIRI("http://localhost/ontology/control/1")),
                //
                VF.createStatement(descRes3,
                        VF.createIRI(
                                "http://plugins.linkedpipes.com/ontology/configuration/property"),
                        VF.createIRI("http://localhost/ontology/value/2")),
                VF.createStatement(descRes3,
                        VF.createIRI(
                                "http://plugins.linkedpipes.com/ontology/configuration/control"),
                        VF.createIRI("http://localhost/ontology/control/2"))
        );
        //
        final Collection<Statement> newConfig = ConfigurationFacade
                .createTemplateConfiguration(configRdf, descRdf,
                        "http://localhost/resource/new", null);
        // TODO Implement check !
//        System.out.println();
//        for (Statement statement : configRdf) {
//            System.out.println(statement.getSubject().stringValue() + " " +
//                    statement.getPredicate().stringValue() + " " +
//                    statement.getObject().stringValue());
//        }
//        System.out.println();
//        for (Statement statement : descRdf) {
//            System.out.println(statement.getSubject().stringValue() + " " +
//                    statement.getPredicate().stringValue() + " " +
//                    statement.getObject().stringValue());
//        }
//        System.out.println();
//        for (Statement statement : newConfig) {
//            System.out.println(statement.getSubject().stringValue() + " " +
//                    statement.getPredicate().stringValue() + " " +
//                    statement.getObject().stringValue());
//        }
    }

    @Test
    public void forcedConfiguration() {

    }

}
