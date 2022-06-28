package com.linkedpipes.plugin.xpipeline;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class Pipeline implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Configuration
    public PipelineConfiguration configuration;

    @Override
    public void execute() throws LpException {
        validateConfiguration();
        List<Statement> body = prepareRequestContent();
        URL url = prepareUrl();
        try {
            executePost(url, body);
        } catch (IOException ex) {
            throw new LpException("Can't execute request.", ex);
        }
    }

    private void validateConfiguration() throws LpException {
        if (configuration.getPipeline() == null ||
                configuration.getPipeline().isBlank()) {
            throw new LpException("Pipeline to execute is not set.");
        }
    }

    private List<Statement> prepareRequestContent() {
        List<Statement> result = new ArrayList<>();
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Resource resource = valueFactory.createBNode();
        result.add(valueFactory.createStatement(
                resource,
                RDF.TYPE,
                valueFactory.createIRI(
                        "http://etl.linkedpipes.com/ontology/ExecutionOptions"
                )));
        result.add(valueFactory.createStatement(
                resource,
                valueFactory.createIRI(
                        "http://linkedpipes.com/ontology/saveDebugData"
                ),
                valueFactory.createLiteral(
                        configuration.isSaveDebugData()
                )));
        result.add(valueFactory.createStatement(
                resource,
                valueFactory.createIRI(
                        "http://linkedpipes.com/ontology/deleteWorkingData"
                ),
                valueFactory.createLiteral(
                        configuration.isDeleteWorkingDirectory()
                )));
        result.add(valueFactory.createStatement(
                resource,
                valueFactory.createIRI(
                        "http://linkedpipes.com/ontology/logPolicy"
                ),
                valueFactory.createIRI(getLogPolicy())));
        result.add(valueFactory.createStatement(
                resource,
                valueFactory.createIRI(
                        "http://linkedpipes.com/ontology/logLevel"
                ),
                valueFactory.createLiteral(configuration.getLogLevel())));
        return result;
    }

    private String getLogPolicy() {
        switch (configuration.getLogPolicy()) {
            case PipelineVocabulary.LOG_PRESERVE:
                return "http://linkedpipes.com/ontology/log/Preserve";
            case PipelineVocabulary.LOG_DELETE_ON_SUCCESS:
                return "http://linkedpipes.com/ontology/log/DeleteOnSuccess";
            default:
                return "http://linkedpipes.com/ontology/log/Preserve";
        }
    }

    private URL prepareUrl() throws LpException {
        String base = encodeUrlForIdn(configuration.getInstance()).toString();
        String pipeline = configuration.getPipeline();
        String url = base + "/api/v1/executions?pipeline=" +
                URLEncoder.encode(pipeline, StandardCharsets.UTF_8);
        try {
            return new URL(url);
        } catch (IOException ex) {
            throw new LpException("Can't create URL: {}", url, ex);
        }
    }

    private URL encodeUrlForIdn(String urlAsString) throws LpException {
        URL url;
        try {
            // Parse so we have access to parts.
            url = new URL(urlAsString);
            // Encode the host to support IDN.
            return new URL(
                    url.getProtocol(),
                    IDN.toASCII(url.getHost()),
                    url.getPort(),
                    url.getFile());
        } catch (IOException ex) {
            throw new LpException("Can't create URL: {}",
                    urlAsString, ex);
        }
    }

    private void executePost(URL url, List<Statement> body)
            throws IOException, LpException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");
            MultipartConnection multipart = new MultipartConnection(connection);
            multipart.addStream(
                    "configuration", "configuration.jsonld", (stream) -> {
                        Rio.write(body, stream, RDFFormat.JSONLD);
                    });
            multipart.finishRequest();
            handleResponse(multipart);
        } finally {
            connection.disconnect();
        }
    }

    private void handleResponse(MultipartConnection connection)
            throws IOException, LpException {
        if (connection.requestFailed()) {
            throw new LpException("Request failed code:{}\nmessage: {}",
                    connection.getResponseCode(),
                    connection.getResponseMessage());
        }
    }

}
