package com.linkedpipes.etl.unpacker.executions;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.linkedpipes.etl.unpacker.UnpackerException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.rdf4j.model.Statement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HttpExecutionSourceTest {

    private static final String EXECUTION_IRI = "http://localhost:8080/resources/executions/69df93d8";

    private static final String JSONLD_RESPONSE = "["
            + "{\"@id\": \"" + EXECUTION_IRI + "\","
            + " \"@type\": [\"http://etl.linkedpipes.com/ontology/Execution\"]}"
            + "]";

    private Server server;

    private int port;

    @BeforeEach
    public void startServer() throws Exception {
        server = new Server(0);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(
                    String target,
                    Request baseRequest,
                    HttpServletRequest request,
                    HttpServletResponse response)
                    throws IOException {
                String iri = request.getParameter("iri");
                if (EXECUTION_IRI.equals(iri)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/ld+json");
                    response.getWriter().write(JSONLD_RESPONSE);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                baseRequest.setHandled(true);
            }
        });
        server.start();
        port = ((org.eclipse.jetty.server.ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    @AfterEach
    public void stopServer() throws Exception {
        server.stop();
    }

    @Test
    public void getExecution() throws Exception {
        HttpExecutionSource source = new HttpExecutionSource("http://localhost:" + port);
        Collection<Statement> statements = source.getExecution(EXECUTION_IRI);
        Assertions.assertFalse(statements.isEmpty());
        Assertions.assertTrue(statements.stream().anyMatch(s -> s.getSubject().stringValue().equals(EXECUTION_IRI)));
    }

    @Test
    public void getExecutionMissing() {
        HttpExecutionSource source = new HttpExecutionSource("http://localhost:" + port);
        assertThrows(UnpackerException.class, () -> source.getExecution("http://localhost:8080/resources/executions/missing"));
    }
}
