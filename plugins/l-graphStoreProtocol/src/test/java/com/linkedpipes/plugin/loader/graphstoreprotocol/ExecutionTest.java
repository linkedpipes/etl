package com.linkedpipes.plugin.loader.graphstoreprotocol;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ExecutionTest extends GraphStoreProtocol {

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecutionTest.class);

    @Test
    public void graphDB() throws Exception {
        String graph = "http://localhost/graph/test";

        configuration = new GraphStoreProtocolConfiguration();
        configuration.setEndpointSelect("http://localhost:7200/repositories/nkod");
        getGraphSize(graph);

//        httpClient = new HttpService(false, null, null, null);
//
//        uploadBlazegraph(
//                "http://localhost:7200/repositories/nkod/rdf-graphs/service",
//                new File("d://Temp/data.ttl"),
//                RDFFormat.TURTLE.getDefaultMIMEType(),
//                graph, true);

    }

}
