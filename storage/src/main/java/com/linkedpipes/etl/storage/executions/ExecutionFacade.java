package com.linkedpipes.etl.storage.executions;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.unpacker.ExecutionSource;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;

@Service
public class ExecutionFacade implements ExecutionSource {

    private Configuration configuration;

    @Autowired
    public ExecutionFacade(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Collection<Statement> getExecution(String executionIri)
            throws BaseException {
        return downloadExecutions(executionIri);
    }

    private Collection<Statement> downloadExecutions(String executionIri)
            throws BaseException {
        // Download and parse information about the execution.
        Collection<Statement> executionRdf;
        HttpClientBuilder builder = HttpClientBuilder.create();
        try (CloseableHttpClient client = builder.build()) {
            final HttpGet request = new HttpGet(
                    getExecutionSourceUrl(executionIri));
            request.addHeader("Accept",
                    RDFFormat.JSONLD.getDefaultMIMEType());
            final HttpResponse response = client.execute(request);
            final int responseCode =
                    response.getStatusLine().getStatusCode();
            if (responseCode < 200 && responseCode > 299) {
                // TODO Check and follow redirects ?
                throw new BaseException("Invalid response code: {} " +
                        " from {}", responseCode, executionIri);
            }
            try (InputStream stream = response.getEntity().getContent()) {
                executionRdf = Rio.parse(stream, "http://localhost/base",
                        RDFFormat.JSONLD);
            }
        } catch (MalformedURLException ex) {
            throw new BaseException("Invalid execution IRI.", ex);
        } catch (IOException ex) {
            throw new BaseException("Can't get mapped execution.", ex);
        }
        return executionRdf;
    }

    private String getExecutionSourceUrl(String iri) {
        String id = iri.substring(iri.lastIndexOf("/") + 1);
        return configuration.getExecutorMonitorUrl() + "executions/" + id;
    }

}
