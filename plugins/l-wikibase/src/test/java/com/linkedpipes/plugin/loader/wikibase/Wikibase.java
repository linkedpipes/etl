package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.test.TestUtils;
import com.linkedpipes.etl.test.suite.Rdf4jSource;
import com.linkedpipes.plugin.loader.wikibase.model.DocumentsLoader;
import com.linkedpipes.plugin.loader.wikibase.model.Property;
import com.linkedpipes.plugin.loader.wikibase.model.WikibaseDocument;
import org.junit.Test;
import org.wikidata.wdtk.datamodel.implementation.DatatypeIdImpl;
import org.wikidata.wdtk.datamodel.implementation.ValueSnakImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

import java.util.HashMap;

public class Wikibase {

    @Test
    public void update() throws Exception {
        ApiConnection connection = new ApiConnection(
                "https://wikibase.opendata.cz/w/api.php");
        connection.login(
                "Klimek@Postman",
                "pbnslciirrtimjr689kgb6n8sbg40ku0");

        Rdf4jSource source = new Rdf4jSource();
        source.loadFile(TestUtils.fileFromResource(
                "update.ttl"));
        DocumentsLoader loader = new DocumentsLoader(
                "https://wikibase.opendata.cz/", source);
        WikibaseDocument document = loader.loadDocument(
                "https://wikibase.opendata.cz/entity/Q2189");

        HashMap<String, Property> ontology = new HashMap<>();
        ontology.put("P13", new Property(null, "http://wikiba.se/ontology#GeoShape"));

        DocumentSynchronizer synchronizer = new DocumentSynchronizer(
                null,
                connection,
                "https://wikibase.opendata.cz/",
                null,
                2000,
                ontology);
        synchronizer.synchronize(document);
        connection.logout();
    }


        @Test
    public void readTest() throws Exception {
        ApiConnection connection = new ApiConnection(
                "https://www.wikidata.org/w/api.php");
        connection.login(
                "Linkedpipes-etl@lpetl",
                "l2q4gft2fl5nhrdsdpvenc62km00lopj");


        WikibaseDataFetcher fetch =
                new WikibaseDataFetcher(
                        connection, "www.wikidata.org/");

        EntityDocument document = fetch.getEntityDocument("Q4115189");
        connection.logout();

        // musical-notation

        ValueSnakImpl snak;
        DatatypeIdImpl impl;
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

    //    @Test
    public void logoutTestWikidata() throws Exception {
        ApiConnection connection = new ApiConnection(
                "https://www.wikidata.org/w/api.php");
        connection.login(
                "Linkedpipes-etl@lpetl",
                "l2q4gft2fl5nhrdsdpvenc62km00lopj");

        connection.logout();
    }

}

