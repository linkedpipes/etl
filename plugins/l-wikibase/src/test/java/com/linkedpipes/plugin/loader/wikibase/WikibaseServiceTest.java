package com.linkedpipes.plugin.loader.wikibase;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ReferenceBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;
import org.wikidata.wdtk.datamodel.interfaces.TermedDocument;
import org.wikidata.wdtk.rdf.PropertyRegister;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataEditor;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WikibaseServiceTest {

//    @Test
    public void updateOpenDataApiTest() throws Exception {
        ApiConnection connection = new ApiConnection(
                "https://wikibase.opendata.cz/w/api.php");
        connection.login(
                "Klimek@Postman",
                "pbnslciirrtimjr689kgb6n8sbg40ku0");

        String siteIri = "https://wikibase.opendata.cz/entity/";
        WikibaseDataEditor wbde = new WikibaseDataEditor(connection, siteIri);
        WikibaseDataFetcher wbdf = new WikibaseDataFetcher(connection, siteIri);

        EntityDocument entityDocument = wbdf.getEntityDocument("Q2189");
        ItemDocument itemDocument = (ItemDocument) entityDocument;
        StatementDocument statementDocument = (StatementDocument) entityDocument;
        TermedDocument termedDocument = (TermedDocument)entityDocument ;

        PropertyIdValue property = Datamodel.makePropertyIdValue("P3", siteIri);

        List<Statement> addStatement = new ArrayList<>();
        List<Statement> deleteStatement = new ArrayList<>();

        addStatement.add(
                StatementBuilder
                        .forSubjectAndProperty(
                                itemDocument.getEntityId(), property)
                        .withValue(Datamodel.makeStringValue("String value"))
                        .build());
//        deleteStatement.add(statementDocument.getAllStatements().next());

        List<String> tags = Arrays.asList("my tag", "my #2");
//        wbde.updateStatements(
//                itemDocument.getEntityId(),
//                addStatement, deleteStatement,
//                "Custom edit message", tags);

//        itemDocument = itemDocument
//                .withLabel(Datamodel.makeMonolingualTextValue("Label", "en"));
//        wbde.editItemDocument(itemDocument, false, "wbde.editItemDocument", null);

        // Given property is used jus to get
        // value of http://wikiba.se/ontology#directClaim
        PropertyRegister register = new PropertyRegister(
                "P10", connection, siteIri);
        register.fetchUsingSPARQL(URI.create(
                "https://wbqs.opendata.cz/bigdata/namespace/wdq/sparql"));

        connection.logout();
    }

//    @Test
    public void updateOpenData() throws Exception {
        ApiConnection connection = new ApiConnection(
                "https://wikibase.opendata.cz/w/api.php");
        connection.login(
                "Klimek@Postman",
                "pbnslciirrtimjr689kgb6n8sbg40ku0");

        String siteIri = "https://wikibase.opendata.cz/entity/";
        WikibaseDataEditor wbde = new WikibaseDataEditor(connection, siteIri);
        WikibaseDataFetcher wbdf = new WikibaseDataFetcher(connection, siteIri);

        EntityDocument entityDocument = wbdf.getEntityDocument("Q2189");
        ItemDocument itemDocument = (ItemDocument)entityDocument;

        StatementBuilder builder = StatementBuilder.forSubjectAndProperty(
                itemDocument.getEntityId(),
                Datamodel.makePropertyIdValue("P3", siteIri));
        // Without ID new statement is created - with ID the statement
        // is updated - values are added.
        builder.withId("Q2189$728DBF5E-8E05-4773-9592-AF8FCFD4A9D6");
        builder.withValue(Datamodel.makeStringValue("String value"));

        // Data in a reference are not preserved.
        // TODO We need to merge them.
        ReferenceBuilder refBuilder = ReferenceBuilder.newInstance();
//        refBuilder.withPropertyValue(
//                Datamodel.makePropertyIdValue("P3", siteIri),
//                Datamodel.makeStringValue("Reference #1"));
//        refBuilder.withPropertyValue(
//                Datamodel.makePropertyIdValue("P3", siteIri),
//                Datamodel.makeStringValue("Reference #2"));
        refBuilder.withPropertyValue(
                Datamodel.makePropertyIdValue("P3", siteIri),
                Datamodel.makeStringValue("Reference #3"));
        builder.withReference(refBuilder.build());

        itemDocument = itemDocument.withStatement(builder.build());
        wbde.editItemDocument(itemDocument, false, "Message", null);

        connection.logout();
    }

//    @Test
    public void readTest() throws Exception {
        ApiConnection connection = new ApiConnection(
                "https://www.wikidata.org/w/api.php");
        connection.login(
                "LinkedPipes ETL Bot@lpetl",
                "l2q4gft2fl5nhrdsdpvenc62km00lopj");

        WikibaseDataFetcher fetch =
                new WikibaseDataFetcher(
                        connection, "www.wikidata.org/");

//        EntityDocument document = fetch.getEntityDocument("Q10768607");

        PropertyRegister register = new PropertyRegister(
                "P1921", connection, "http://www.wikidata.org/entity/");
        register.fetchUsingSPARQL(URI.create(
                "https://wbqs.opendata.cz/bigdata/namespace/wdq/sparql"));

        connection.logout();
    }

    //    @Test
    public void logoutTestFromOpendata() throws Exception {
        ApiConnection connection = new ApiConnection(
                "https://wikibase.opendata.cz/w/api.php");
        connection.login(
                "Klimek@Postman",
                "pbnslciirrtimjr689kgb6n8sbg40ku0");

        connection.logout();
    }

}
