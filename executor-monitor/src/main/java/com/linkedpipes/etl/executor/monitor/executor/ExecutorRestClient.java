package com.linkedpipes.etl.executor.monitor.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
class ExecutorRestClient {

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecutorRestClient.class);

    private final RestTemplate restTemplate;

    public ExecutorRestClient() {
        restTemplate = new RestTemplate();
        // Support national character encoding in messages.
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    /**
     * Can return null as an empty body.
     */
    public String check(Executor executor) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        HttpEntity entity = new HttpEntity<>(headers);
        headers.add("Accept", "application/json");
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    getOverviewUrl(executor), HttpMethod.GET,
                    entity, String.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == HttpStatus.SC_NOT_FOUND) {
                // There is no execution running, that is fine.
                return null;
            } else {
                throw ex;
            }
        }
        return response.getBody();
    }

    private String getOverviewUrl(Executor executor) {
        return executor.getAddress() + "/api/v1/executions/overview";
    }

    public void start(Executor executor, Execution execution)
            throws MonitorException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");

        String body = createStartExecutionBody(execution);
        HttpEntity request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getExecutionsUrl(executor), HttpMethod.POST,
                request, String.class);

        LOG.info("Starting execution: '{}' on '{}' -> {}",
                execution.getIri(),
                executor.getAddress(),
                response.getStatusCode());

        if (response.getStatusCode().value() != HttpStatus.SC_CREATED) {
            throw new MonitorException("Can't start execution, response: {}",
                    response.getStatusCode());
        }
    }

    private String getExecutionsUrl(Executor executor) {
        return executor.getAddress() + "/api/v1/executions";
    }

    private String createStartExecutionBody(Execution execution)
            throws MonitorException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("iri", execution.getIri());
        root.put(
                "directory",
                execution.getDirectory().toString().replace("\\", "/"));
        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            throw new MonitorException("Can't serialize request.", ex);
        }
    }

    public void cancel(Executor executor, String userRequest)
            throws MonitorException {
        try {
            this.cancelExecution(executor, userRequest);
        } catch (Exception ex) {
            throw new MonitorException("Cancel request failed.", ex);
        }
    }

    private void cancelExecution(Executor executor, String userRequest) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        HttpEntity request = new HttpEntity<>(userRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                getCancelUrl(executor), HttpMethod.POST,
                request, String.class);

        LOG.info("Cancelling '{}' -> {}",
                executor.getAddress(), response.getStatusCode());
    }

    private String getCancelUrl(Executor executor) {
        return executor.getAddress() + "/api/v1/executions/cancel";
    }

}
