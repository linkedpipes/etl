package com.linkedpipes.etl.executor.monitor.executor;

import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ExecutorFacade {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutorFacade.class);

    @Autowired
    private ExecutionFacade executionFacade;

    @Autowired
    private Configuration configuration;

    private final List<Executor> executors = new ArrayList<>(2);

    private final RestTemplate restTemplate = new RestTemplate();

    private final Object startLock = new Object();

    @PostConstruct
    protected void onInit() {
        // Add a single executor.
        addExecutor(configuration.getExecutorUri());
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 500)
    protected void check() {
        for (Executor executor : executors) {
            check(executor);
        }
        //
        startExecutions();
    }

    /**
     * Get executions waiting to execute and try to start them.
     */
    public void startExecutions() {
        synchronized (startLock) {
            final Collection<Execution> queued
                    = executionFacade.getExecutionsQueued();
            if (queued.isEmpty()) {
                return;
            }
            final Iterator<Execution> iterator = queued.iterator();
            for (Executor executor : executors) {
                if (executor.isAlive() && executor.getExecution() == null) {
                    try {
                        final Execution execution = iterator.next();
                        startExecution(execution, executor);
                        if (!iterator.hasNext()) {
                            return;
                        }
                    } catch (Exception ex) {
                        LOG.error("Can't start execution.", ex);
                    }
                }
            }
        }
    }

    /**
     * Try to start given execution on given executor.
     * <p>
     * If execution is started the reference is set to executor as well
     * as an execution.
     *
     * @param execution
     * @param executor
     */
    private void startExecution(Execution execution, Executor executor)
            throws Exception {
        LOG.info("startExecution: {} on {}", execution.getIri(),
                executor.getAddress());
        final MultiValueMap<String, String> headers
                = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");

        final StringBuilder body = new StringBuilder();
        body.append("{\"iri\":\"");
        body.append(execution.getIri());
        body.append("\",\"directory\":\"");
        // Windows use \ in path - we need to get rid of this character.
        body.append(execution.getDirectory().toString().replace("\\", "/"));
        body.append("\"}");

        final HttpEntity request = new HttpEntity<>(
                body.toString(), headers);

        final ResponseEntity<String> response = restTemplate.exchange(
                executor.getAddress() + "/api/v1/executions",
                HttpMethod.POST,
                request,
                String.class);

        executor.setExecution(execution);
        LOG.info("   reponse status code: {}", response.getStatusCode());
        // Add references.
        executor.setExecution(execution);
        executionFacade.attachExecutor(execution);
    }

    /**
     * Add new executor and perform initial check.
     *
     * @param address
     */
    private void addExecutor(String address) {
        final Executor executor = new Executor(address);
        check(executor);
        executors.add(executor);
    }

    /**
     * Use HTTP to check the executor status and running pipeline.
     * <p>
     * Can be used for initialization as well as an update.
     *
     * @param executor
     */
    private void check(Executor executor) {
        // Request data in JSONLD format.
        final MultiValueMap<String, String> headers
                = new LinkedMultiValueMap<>();
        headers.add("Accept", "application/ld+json");
        final ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    executor.getAddress() + "/api/v1/executions",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            executor.setAlive(true);
        } catch (Exception ex) {
            if (executor.isAlive()) {
                // Print error only if we lost the connection for the
                // first time.
                LOG.error("Can't connect to: {}", executor.getAddress(), ex);
            }
            executor.setAlive(false);
            // Set execution to unresponsive state.
            if (executor.getExecution() != null) {
                executionFacade.unresponsiveExecutor(executor.getExecution());
            }
            return;
        }
        // Check if there was any response.
        final String body = response.getBody();
        if (body == null) {
            // Is not executing -> detach any possibly attached execution.
            if (executor.getExecution() != null) {
                // However the execution may finished in mean time, so
                // we need to check it from the disk first.
                try {
                    executionFacade.updateFromFile(executor.getExecution());
                } catch (ExecutionFacade.OperationFailed |
                        ExecutionFacade.ExecutionMismatch ex) {
                    LOG.warn("Can't update from file before detach. {}",
                            executor.getExecution().getId());
                }
                //
                executionFacade.detachExecutor(executor.getExecution());
                executor.setExecution(null);
            }
            executor.setLastCheck(new Date());
            return;
        }
        // Parse the response.
        final InputStream stream = new ByteArrayInputStream(
                body.getBytes(StandardCharsets.UTF_8));
        try {
            if (executor.getExecution() == null) {
                executor.setExecution(executionFacade.discover(stream));
                executionFacade.attachExecutor(executor.getExecution());
            } else {
                // Update execution from stream.
                executionFacade.update(executor.getExecution(), stream);
            }
            executor.setLastCheck(new Date());
        } catch (ExecutionFacade.UnknownExecution | ExecutionFacade.ExecutionMismatch ex) {
            // The execution in the stream is unknown. Detach the execution
            // and wait for other refresh.
            if (executor.getExecution() != null) {
                executionFacade.detachExecutor(executor.getExecution());
                executor.setExecution(null);
            }
        } catch (ExecutionFacade.OperationFailed ex) {
            // Unset execution, it will be discovered in the next check.
            if (executor.getExecution() != null) {
                executionFacade.detachExecutor(executor.getExecution());
                executor.setExecution(null);
            }
        }
    }

}
