package com.linkedpipes.etl.executor.monitor.execution.resource;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.executor.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceReader {

    private static final Logger LOG =
            LoggerFactory.getLogger(ResourceReader.class);

    public void update(Execution execution, LoadableResource resource) {
        final File file = new File(execution.getDirectory(),
                resource.getRelativeFilePath());
        if (!file.exists()) {
            // Used for non initialized executions.
            resource.missing(execution);
            return;
        }
        try (InputStream stream = new FileInputStream(file)) {
            resource.load(stream);
        } catch (IOException ex) {
            LOG.error("Can't load resource from: {}", file, ex);
        }
    }

    public void update(Execution execution, LoadableResource resource,
            Executor executor) {
        final String url = executor.getAddress() + resource.getRelativeUrlPath();
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<org.springframework.core.io.Resource> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    org.springframework.core.io.Resource.class);
        } catch (RestClientException ex) {
            LOG.error("Can't request executor data", ex);
            return;
        }
        // We do not need to close the stream.
        final InputStream responseStream;
        try {
            responseStream = response.getBody().getInputStream();
            resource.load(responseStream);
        }
        catch (IOException ex) {
            LOG.error("Can't read response stream", ex);
        }
    }

}
