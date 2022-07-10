package com.linkedpipes.etl.storage.unpacker.executions;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.Configuration;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

class HttpExecutionSource {

    private static final String BASE_URL = "http://localhost/base";

    private final Configuration configuration;

    HttpExecutionSource(Configuration configuration) {
        this.configuration = configuration;
    }

    public Collection<Statement> downloadExecution(String iri)
            throws StorageException {
        // Download and parse information about the execution.
        HttpClientBuilder builder = HttpClientBuilder.create();
        try (CloseableHttpClient client = builder.build()) {
            HttpGet request = new HttpGet(getExecutionSourceUrl(iri));
            request.addHeader("Accept", RDFFormat.JSONLD.getDefaultMIMEType());
            HttpResponse response = client.execute(request);
            checkResponse(response, iri);
            return responseToRdf(response);
        } catch (MalformedURLException ex) {
            throw new StorageException("Invalid execution IRI.", ex);
        } catch (IOException ex) {
            throw new StorageException("Can't get mapped execution.", ex);
        }
    }

    private String getExecutionSourceUrl(String iri) {
        return configuration.getExecutorMonitorUrl() + "executions/?iri="
                + URLEncoder.encode(iri, StandardCharsets.UTF_8);
    }

    private void checkResponse(HttpResponse response, String iri)
            throws StorageException {
        int status = response.getStatusLine().getStatusCode();
        if (status < HttpStatus.SC_OK
                || status >= HttpStatus.SC_MULTIPLE_CHOICES) {
            throw new StorageException(
                    "Invalid response code: {} from {}", status, iri);
        }
    }

    private Collection<Statement> responseToRdf(HttpResponse response)
            throws IOException {
        try (InputStream stream = response.getEntity().getContent()) {
            return Rio.parse(stream, BASE_URL, RDFFormat.JSONLD);
        }
    }

}
