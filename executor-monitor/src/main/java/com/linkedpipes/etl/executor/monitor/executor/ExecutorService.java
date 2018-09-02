package com.linkedpipes.etl.executor.monitor.executor;

import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExecutorService {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutorService.class);

    private final ExecutionFacade executionFacade;

    private final Configuration configuration;

    private final List<Executor> executors = new ArrayList<>(2);

    private final RestTemplate restTemplate = new RestTemplate();

    private final Object startLock = new Object();

    private final HttpCheckExecutor checker;

    @Autowired
    public ExecutorService(
            ExecutionFacade executionFacade, Configuration configuration) {
        this.executionFacade = executionFacade;
        this.configuration = configuration;
        this.checker = new HttpCheckExecutor(executionFacade);
    }

    @PostConstruct
    public void onInit() throws MonitorException {
        this.addExecutors();
    }

    private void addExecutors() throws MonitorException {
        this.addExecutor(this.configuration.getExecutorUri());
    }

    private void addExecutor(String address) throws MonitorException {
        Executor executor = new Executor(address);
        this.checkExecutor(executor);
        this.executors.add(executor);
    }

    private void checkExecutor(Executor executor) throws MonitorException {
        this.checker.check(executor);
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 500)
    public void checkExecutors() throws MonitorException {
        for (Executor executor : this.executors) {
            this.checkExecutor(executor);
        }
        this.startExecutions();
    }

    @Async
    public void asynchStartExecutions() {
        this.startExecutions();
    }

    public void startExecutions() {
        synchronized (this.startLock) {
            List<Execution> queued = this.getExecutionQueued();
            if (queued.isEmpty()) {
                return;
            }
            Iterator<Execution> iterator = queued.iterator();
            for (Executor executor : executors) {
                if (executor.isAlive() && executor.getExecution() == null) {
                    try {
                        startExecution(iterator.next(), executor);
                    } catch (Exception ex) {
                        LOG.error("Can't start execution.", ex);
                    }
                }
                if (!iterator.hasNext()) {
                    return;
                }
            }
        }
    }

    private List<Execution> getExecutionQueued() {
        return this.executionFacade.getExecutions().stream()
                .filter(ex -> ExecutionStatus.QUEUED.equals(ex.getStatus()))
                .sorted((l, r) -> l.getIri().compareTo(r.getIri()))
                .collect(Collectors.toList());
    }

    public void cancelExecution(Execution execution, String userRequest)
            throws MonitorException {
        Executor executor = this.getExecutor(execution);
        if (executor == null) {
            throw new MonitorException(
                    "Can't find execution for: {}", execution.getIri());
        }
        try {
            this.cancelExecutor(executor, userRequest);
        } catch (Exception ex) {
            throw new MonitorException("Cancel request failed.", ex);
        }
    }

    private Executor getExecutor(Execution execution) {
        for (Executor executor : this.executors) {
            if (execution.equals(executor.getExecution())) {
                return executor;
            }
        }
        return null;
    }

    private void cancelExecutor(Executor executor, String userRequest) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        HttpEntity request = new HttpEntity<>(userRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                executor.getAddress() + "/api/v1/executions/cancel",
                HttpMethod.POST,
                request,
                String.class);

        LOG.info("Cancelling '{}' response: {}", executor.getAddress(),
                response.getStatusCode());
    }

    private void startExecution(Execution execution, Executor executor) {
        LOG.info("Starting execution: '{}' on '{}'",
                execution.getIri(), executor.getAddress());

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");

        String body = this.createStartExecutionBody(execution);
        HttpEntity request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = this.restTemplate.exchange(
                executor.getAddress() + "/api/v1/executions",
                HttpMethod.POST,
                request,
                String.class);

        executor.setExecution(execution);
        LOG.info("Executor '{}' response: {}",
                executor.getAddress(), response.getStatusCode());

        this.executionFacade.onAttachExecutor(execution, executor);
    }

    private String createStartExecutionBody(Execution execution) {
        StringBuilder body = new StringBuilder();
        body.append("{\"iri\":\"");
        body.append(execution.getIri());
        body.append("\",\"directory\":\"");
        // Windows use \ in path - we need to get rid of this character.
        body.append(execution.getDirectory().toString().replace("\\", "/"));
        body.append("\"}");
        return body.toString();
    }

}
