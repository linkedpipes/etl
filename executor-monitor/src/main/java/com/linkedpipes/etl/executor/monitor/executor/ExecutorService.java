package com.linkedpipes.etl.executor.monitor.executor;

import com.linkedpipes.etl.executor.monitor.Configuration;
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

    private final Configuration configuration;

    private final List<Executor> executors = new ArrayList<>(2);

    private final Object startLock = new Object();

    private final ExecutorRestClient restClient;

    private final ExecutorUpdater checker;

    @Autowired
    public ExecutorService(
            ExecutionSource executions,
            ExecutorEventListener eventListener,
            Configuration configuration,
            ExecutorRestClient restClient,
            ExecutorUpdater checker) {
        this.executions = executions;
        this.eventListener = eventListener;
        this.configuration = configuration;
        this.restClient = restClient;
        this.checker = checker;
    }

    @PostConstruct
    public void onInit() {
        this.addExecutor(this.configuration.getExecutorUri());
    }

    private void addExecutor(String address) {
        Executor executor = new Executor(address);
        this.checkExecutor(executor);
        this.executors.add(executor);
    }

    private void checkExecutor(Executor executor) {
        this.checker.update(executor);
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 500)
    public void checkExecutors() {
        for (Executor executor : this.executors) {
            this.checkExecutor(executor);
        }
        this.startExecutions();
    }

    /**
     * Can be used by an external caller otherwise the annotation is not used.
     */
    @Async
    public void asyncStartExecutions() {
        this.startExecutions();
    }

    private void startExecutions() {
        synchronized (this.startLock) {
            List<Execution> queued = this.getExecutionQueued();
            if (queued.isEmpty()) {
                return;
            }
            Iterator<Execution> iterator = queued.iterator();
            for (Executor executor : executors) {
                if (executor.isAvailableForNewExecution()) {
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
        return this.executions.getExecutions().stream()
                .filter(ex -> ExecutionStatus.QUEUED.equals(ex.getStatus()))
                .sorted((l, r) -> l.getIri().compareTo(r.getIri()))
                .collect(Collectors.toList());
    }

    private void startExecution(Execution execution, Executor executor) {
        this.restClient.start(executor, execution);
        this.eventListener.onAttachExecutor(execution, executor);
    }

    public void cancelExecution(Execution execution, String userRequest)
            throws MonitorException {
        Executor executor = execution.getExecutor();
        if (executor == null) {
            throw new MonitorException(
                    "Can't find execution for: {}", execution.getIri());
        }
        this.restClient.cancel(executor, userRequest);
    }

}
