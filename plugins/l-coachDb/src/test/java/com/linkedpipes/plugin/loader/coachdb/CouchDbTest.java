package com.linkedpipes.plugin.loader.coachdb;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class CouchDbTest {

//    @Test
    public void testLocal() throws LpException {
        CouchDb db = new CouchDb("http://127.0.0.1:5984",
                (message, args) -> new LpException(message, args));

        db.deleteDatabase("datasets");

        db.createDatabase("datasets");

        db.uploadDocuments("datasets", Arrays.asList(new File(
                "D:/Projects/nkod/loading-couchdb/83037.jsonld")));
    }

}
