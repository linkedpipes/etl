package com.linkedpipes.etl.executor.monitor.executor;

import com.linkedpipes.etl.executor.monitor.ConfigurationHolder;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExecutorService {

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutorService.class);

    private final ExecutionSource executions;

    private final ExecutorEventListener eventListener;

    private final ConfigurationHolder configuration;

    private final List<Executor> executors = new ArrayList<>(2);

    private final Object startLock = new Object();

    private final ExecutorRestClient restClient;

    private final CheckExecutor checker;

    @Autowired
    public ExecutorService(
            ExecutionSource executions,
            ExecutorEventListener eventListener,
            ConfigurationHolder configuration,
            ExecutorRestClient restClient,
            CheckExecutor checker) {
        this.executions = executions;
        this.eventListener = eventListener;
        this.configuration = configuration;
        this.restClient = restClient;
        this.checker = checker;
    }

    @PostConstruct
    public void onInit() {
        addExecutor(configuration.getExecutorUri());
    }

    private void addExecutor(String address) {
        Executor executor = new Executor(address);
        checkExecutor(executor);
        executors.add(executor);
    }

    private void checkExecutor(Executor executor) {
        checker.check(executor);
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 500)
    public void checkExecutors() {
        for (Executor executor : executors) {
            checkExecutor(executor);
        }
        startExecutions();
    }

    /**
     * Must be called from outside else the annotation is not used.
     */
    @Async
    public void asyncStartExecutions() {
        startExecutions();
    }

    private void startExecutions() {
        synchronized (startLock) {
            List<Execution> queued = getExecutionQueued();
            if (queued.isEmpty()) {
                return;
            }
            Iterator<Execution> iterator = queued.iterator();
            for (Executor executor : executors) {
                if (isAvailableForNewExecution(executor)) {
                    startExecution(iterator.next(), executor);
                }
                if (!iterator.hasNext()) {
                    return;
                }
            }
        }
    }

    private List<Execution> getExecutionQueued() {
        return executions.getExecutions().stream()
                .filter(ex -> ExecutionStatus.QUEUED.equals(ex.getStatus()))
                .sorted((l, r) -> l.getIri().compareTo(r.getIri()))
                .collect(Collectors.toList());
    }

    private boolean isAvailableForNewExecution(Executor executor) {
        return executor.isAlive() && executions.getExecution(executor) == null;
    }

    private void startExecution(Execution execution, Executor executor) {
        try {
            this.restClient.start(executor, execution);
            this.eventListener.onExecutorHasExecution(execution, executor);
        } catch (Exception ex) {
            LOG.error("Can't start execution.", ex);
        }
    }

    public void cancelExecution(Execution execution, String userRequest)
            throws MonitorException {
        Executor executor = getExecutor(execution);
        if (executor == null) {
            throw new MonitorException(
                    "Can't find executor for: {}", execution.getIri());
        }
        this.restClient.cancel(executor, userRequest);
    }

    private Executor getExecutor(Execution execution) {
        for (Executor executor : executors) {
            Execution executorsExecution = executions.getExecution(executor);
            if (execution == executorsExecution) {
                return executor;
            }
        }
        return null;
    }

}
