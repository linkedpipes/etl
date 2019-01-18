package com.linkedpipes.etl.storage.executions;

import com.linkedpipes.etl.storage.BaseException;
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
import java.util.Collection;

class HttpExecutionSource {

    private static final String BASE_URL = "http://localhost/base";

    private final Configuration configuration;

    HttpExecutionSource(Configuration configuration) {
        this.configuration = configuration;
    }

    Collection<Statement> downloadExecution(String iri) throws BaseException {
        // Download and parse information about the execution.
        HttpClientBuilder builder = HttpClientBuilder.create();
        try (CloseableHttpClient client = builder.build()) {
            HttpGet request = new HttpGet(getExecutionSourceUrl(iri));
            request.addHeader("Accept", RDFFormat.JSONLD.getDefaultMIMEType());
            HttpResponse response = client.execute(request);
            checkResponse(response, iri);
            return responseToRdf(response);
        } catch (MalformedURLException ex) {
            throw new BaseException("Invalid execution IRI.", ex);
        } catch (IOException ex) {
            throw new BaseException("Can't get mapped execution.", ex);
        }
    }

    private String getExecutionSourceUrl(String iri) {
        String id = iri.substring(iri.lastIndexOf("/") + 1);
        return configuration.getExecutorMonitorUrl() + "executions/" + id;
    }

    private void checkResponse(HttpResponse response, String iri)
            throws BaseException {
        int status = response.getStatusLine().getStatusCode();
        if (status < HttpStatus.SC_OK
                || status >= HttpStatus.SC_MULTIPLE_CHOICES) {
            throw new BaseException(
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
