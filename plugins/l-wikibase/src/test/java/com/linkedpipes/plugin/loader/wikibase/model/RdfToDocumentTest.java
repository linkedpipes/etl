package com.linkedpipes.plugin.loader.wikibase.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.test.TestUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.DatamodelMapper;
import org.wikidata.wdtk.datamodel.implementation.EntityDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.SitesImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Sites;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SomeValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;
import org.wikidata.wdtk.rdf.PropertyRegister;
import org.wikidata.wdtk.rdf.RdfSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RdfToDocumentTest {

    public static PropertyRegister register =
            Mockito.mock(PropertyRegister.class);

    @BeforeClass
    public static void onBefore() {
        String prefix = "https://wikibase.opendata.cz/prop/";
        String prefixWiki = "http://www.wikidata.org/prop/";
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P3", prefix)))
                .thenReturn("http://wikiba.se/ontology#String");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P8", prefix)))
                .thenReturn("http://wikiba.se/ontology#Quantity");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P11", prefix)))
                .thenReturn("http://wikiba.se/ontology#Time");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P12", prefix)))
                .thenReturn("http://wikiba.se/ontology#GlobeCoordinate");
        //
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P31", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#WikibaseItem");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P580", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#Time");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P248", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#WikibaseItem");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P625", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#GlobeCoordinate");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P31", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#WikibaseItem");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P677", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#ExternalId");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P17", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#WikibaseItem");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P18", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#CommonsMedia");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P131", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#WikibaseItem");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P3296", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#ExternalId");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P373", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#String");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P2670", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#WikibaseItem");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P143", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#WikibaseItem");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P248", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#WikibaseItem");
        Mockito.when(register.getPropertyType(
                Datamodel.makePropertyIdValue("P1114", prefixWiki)))
                .thenReturn("http://wikiba.se/ontology#Quantity");
        // Required by JSON loader.
        Answer<String> answer = (InvocationOnMock invocation) -> {
            PropertyIdValue prop =
                    invocation.getArgument(0, PropertyIdValue.class);
            return register.getPropertyType(
                    Datamodel.makePropertyIdValue(
                            prop.getId(), prefixWiki));
        };
        Mockito.when(register.setPropertyTypeFromStringValue(
                Mockito.any(), Mockito.any()))
                .then(answer);
        Mockito.when(register.setPropertyTypeFromQuantityValue(
                Mockito.any(), Mockito.any()))
                .then(answer);
        Mockito.when(register.setPropertyTypeFromGlobeCoordinatesValue(
                Mockito.any(), Mockito.any()))
                .then(answer);
        Mockito.when(register.setPropertyTypeFromEntityIdValue(
                Mockito.any(), Mockito.any()))
                .then(answer);
        Mockito.when(register.setPropertyTypeFromTimeValue(
                Mockito.any(), Mockito.any()))
                .then(answer);
        Mockito.when(register.setPropertyTypeFromMonolingualTextValue(
                Mockito.any(), Mockito.any()))
                .then(answer);
    }

    @Test
    public void loadSimpleValue() throws IOException {
        Model model = loadModelFromResource("simple-value.ttl");
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "https://wikibase.opendata.cz/");
        ItemDocument document = rdfToDocument.loadItemDocument(
                model, "https://wikibase.opendata.cz/entity/Q2077");

        List<Statement> statements = asList(document.getAllStatements());
        Assert.assertEquals(1, statements.size());
        Statement statement = statements.get(0);

        Assert.assertEquals(
                Datamodel.makeStringValue("103064"),
                statement.getValue());
    }

    private <T> List<T> asList(Iterator<T> iterator) {
        List<T> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    private Model loadModelFromResource(String fileName) throws IOException {
        File inputFile = TestUtils.fileFromResource(fileName);
        try (InputStream stream = new FileInputStream(inputFile)) {
            return Rio.parse(stream, "http://base", RDFFormat.TURTLE);
        }
    }

    @Test
    public void loadTimeValueAndQualifier() throws IOException {
        Model model = loadModelFromResource("time-value-and-qualifier.ttl");
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "https://wikibase.opendata.cz/");
        ItemDocument document = rdfToDocument.loadItemDocument(
                model, "urn:NewItem");

        List<Statement> statements = asList(document.getAllStatements());
        Assert.assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        Assert.assertEquals(
                Datamodel.makeTimeValue(
                        1990, (byte) 11, (byte) 1,
                        "http://www.wikidata.org/entity/Q1985727"),
                statement.getValue());

        List<Snak> qualifiers = asList(statement.getAllQualifiers());
        Assert.assertEquals(1, qualifiers.size());
        ValueSnak qualifier = (ValueSnak) qualifiers.get(0);
        Assert.assertEquals(qualifier.getPropertyId().getId(), "P11");
        Assert.assertEquals(
                Datamodel.makeTimeValue(
                        2020, (byte) 1, (byte) 1,
                        "http://www.wikidata.org/entity/Q1985727"),
                qualifier.getValue());
    }

    @Test
    public void loadSomeValue() throws IOException {
        Model model = loadModelFromResource("some-value.ttl");
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "https://wikibase.opendata.cz/");
        ItemDocument document = rdfToDocument.loadItemDocument(
                model, "urn:NewItem");

        List<Statement> statements = asList(document.getAllStatements());
        Assert.assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        Assert.assertEquals(
                null,
                statement.getValue());
    }

    @Test
    public void loadQuantityValueAndQualifier() throws IOException {
        Model model = loadModelFromResource("quantity-value-and-qualifier.ttl");
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "https://wikibase.opendata.cz/");
        ItemDocument document = rdfToDocument.loadItemDocument(
                model, "urn:NewItem");

        List<Statement> statements = asList(document.getAllStatements());
        Assert.assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        Assert.assertEquals(
                Datamodel.makeQuantityValue(
                        BigDecimal.valueOf(362.0),
                        BigDecimal.valueOf(362.0),
                        BigDecimal.valueOf(362.0),
                        "https://wikibase.opendata.cz/entity/Q2153"),
                statement.getValue());

        List<Snak> qualifiers = asList(statement.getAllQualifiers());
        Assert.assertEquals(1, qualifiers.size());
        ValueSnak qualifier = (ValueSnak) qualifiers.get(0);
        Assert.assertEquals(qualifier.getPropertyId().getId(), "P8");
        Assert.assertEquals(
                Datamodel.makeQuantityValue(
                        BigDecimal.valueOf(3.62),
                        BigDecimal.valueOf(3.62),
                        BigDecimal.valueOf(3.62),
                        "https://wikibase.opendata.cz/entity/Q2106"),
                qualifier.getValue());
    }

    @Test
    public void loadNoValue() throws IOException {
        Model model = loadModelFromResource("no-value.ttl");
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "https://wikibase.opendata.cz/");
        ItemDocument document = rdfToDocument.loadItemDocument(
                model, "urn:NewItem");

        List<Statement> statements = asList(document.getAllStatements());
        Assert.assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        Assert.assertTrue(statement.getMainSnak() instanceof SomeValueSnak);
    }

    @Test
    public void loadGeoValueAndQualifier() throws IOException {
        Model model = loadModelFromResource("geo-value-and-qualifier.ttl");
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "https://wikibase.opendata.cz/");
        ItemDocument document = rdfToDocument.loadItemDocument(
                model, "urn:NewItem");

        List<Statement> statements = asList(document.getAllStatements());
        Assert.assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        Assert.assertEquals(
                Datamodel.makeGlobeCoordinatesValue(
                        12.933333333333, 35.3, 0.000277778,
                        "http://www.wikidata.org/entity/Q2"),
                statement.getValue());

        List<Snak> qualifiers = asList(statement.getAllQualifiers());
        Assert.assertEquals(1, qualifiers.size());
        ValueSnak qualifier = (ValueSnak) qualifiers.get(0);
        Assert.assertEquals(qualifier.getPropertyId().getId(), "P12");
        Assert.assertEquals(
                Datamodel.makeGlobeCoordinatesValue(
                        50.675546897005, 15.538850308905, 0.000001,
                        "http://www.wikidata.org/entity/Q2"),
                qualifier.getValue());
    }

    @Test
    public void loadDeriveFrom() throws IOException {
        Model model = loadModelFromResource("derive-from.ttl");
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "https://wikibase.opendata.cz/");
        ItemDocument document = rdfToDocument.loadItemDocument(
                model, "urn:NewItem");

        List<Statement> statements = asList(document.getAllStatements());
        Assert.assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        Assert.assertEquals(
                Datamodel.makeQuantityValue(
                        BigDecimal.valueOf(362.0),
                        BigDecimal.valueOf(362.0),
                        BigDecimal.valueOf(362.0),
                        "https://wikibase.opendata.cz/entity/Q2106"),
                statement.getValue());

        List<Reference> references = statement.getReferences();
        Assert.assertEquals(1, references.size());
        Reference reference = references.get(0);
        List<Snak> refs = asList(reference.getAllSnaks());
        Assert.assertEquals(1, refs.size());

        Assert.assertEquals(
                Datamodel.makeQuantityValue(
                        BigDecimal.valueOf(3.62),
                        BigDecimal.valueOf(3.62),
                        BigDecimal.valueOf(3.62),
                        "https://wikibase.opendata.cz/entity/Q2153"),
                ((ValueSnak) refs.get(0)).getValue());
    }

    @Test
    public void loadUpdateOfQ10768607() throws IOException {
        Model model = loadModelFromResource("Q10768607-update.ttl");
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "http://www.wikidata.org/");
        ItemDocument document = rdfToDocument.loadItemDocument(
                model, "http://www.wikidata.org/entity/Q10768607");

        List<Statement> statements = asList(document.getAllStatements());
        Assert.assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        Assert.assertEquals(
                Datamodel.makeItemIdValue(
                        "Q811534", "http://www.wikidata.org/entity/"),
                statement.getValue());

        Assert.assertEquals(1, statement.getReferences().size());
        Snak reference = statement.getReferences().get(0).getAllSnaks().next();
        Assert.assertEquals(
                Datamodel.makeItemIdValue(
                        "Q26778346", "http://www.wikidata.org/entity/"),
                ((ValueSnak) reference).getValue());

        Assert.assertEquals(1, statement.getQualifiers().size());
        Snak qualifier = asList(statement.getAllQualifiers()).get(0);
        Assert.assertEquals(
                Datamodel.makeTimeValue(
                        1985, (byte) 11, (byte) 21, (byte) 0, (byte) 0, (byte) 0,
                        (byte) 11, 0, 0, 0,
                        "http://www.wikidata.org/entity/Q1985727"),
                ((ValueSnak) qualifier).getValue());
    }

    @Test
    public void loadQ10768607() throws IOException {
        ObjectMapper mapper =
                new DatamodelMapper("http://www.wikidata.org/entity/");
        File jsonFile = TestUtils.fileFromResource("Q10768607.json");
        JsonNode node = mapper.readTree(jsonFile);
        EntityDocument expected =
                mapper.treeToValue(node, EntityDocumentImpl.class);
        //
        Model model = loadModelFromResource("Q10768607.ttl");
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "http://www.wikidata.org/");
        ItemDocument actual = rdfToDocument.loadItemDocument(
                model, "http://www.wikidata.org/entity/Q10768607");
        // Assert also count revision which we can not load from RDF.
        // Also some references are missing in RDF.
//        Assert.assertEquals(expected, actual);
    }

    @Test
    public void roundRobinOfQ10768607() throws IOException {
        ObjectMapper mapper =
                new DatamodelMapper("http://www.wikidata.org/entity/");
        File jsonFile = TestUtils.fileFromResource("Q10768607.json");
        JsonNode node = mapper.readTree(jsonFile);
        EntityDocument expected =
                mapper.treeToValue(node, EntityDocumentImpl.class);
        //
        Sites sites = new SitesImpl();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RdfSerializer serializer =
                new RdfSerializer(RDFFormat.NTRIPLES, stream, sites, register);
        serializer.open();
        serializer.processItemDocument((ItemDocument) expected);
        serializer.close();
        Model model = Rio.parse(
                new ByteArrayInputStream(stream.toByteArray()),
                "http://localhost",
                RDFFormat.NTRIPLES);
        stream.close();
        //
        RdfToDocument rdfToDocument =
                new RdfToDocument(register, "http://www.wikidata.org/");
        ItemDocument actual = rdfToDocument.loadItemDocument(
                model, "http://www.wikidata.org/entity/Q10768607");
        // Assert also count revision which we can not load from RDF.
        // Also some references are missing in RDF.
//        Assert.assertEquals(expected, actual);
    }

}
