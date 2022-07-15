package com.linkedpipes.etl.executor.monitor.events;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LimitCountHistory implements EventListener {

    private static final Logger LOG =
            LoggerFactory.getLogger(LimitCountHistory.class);

    private final List<ExecutionStatus> ignoredStates = Arrays.asList(
            ExecutionStatus.QUEUED,
            ExecutionStatus.RUNNING,
            ExecutionStatus.CANCELLING,
            ExecutionStatus.UNRESPONSIVE,
            ExecutionStatus.DELETED);

    private final Integer historyLimit;

    private ExecutionFacade executionFacade = null;

    public LimitCountHistory(Integer historyLimit) {
        LOG.info("Using history count limit with limit: {}", historyLimit);
        this.historyLimit = historyLimit;
    }

    @Override
    public void onExecutionFacadeReady(ExecutionFacade executions) {
        EventListener.super.onExecutionFacadeReady(executions);
        executionFacade = executions;
        pruneOnStartup();
    }

    private void pruneOnStartup() {
        List<Execution> executions = executionFacade.getExecutions().stream()
                .filter(exec -> !ignoredStates.contains(exec.getStatus()))
                .sorted(Comparator.comparing(Execution::getLastOverviewChange)
                        .reversed())
                .toList();
        Map<Resource, Integer> counter = new HashMap<>();
        for (Execution execution : executions) {
            Resource pipeline = execution.getPipeline();
            counter.put(pipeline, counter.getOrDefault(pipeline, 0) + 1);
            Integer count = counter.get(pipeline);
            if (count > historyLimit) {
                deleteExecution(execution);
            }
        }
    }

    private void deleteExecution(Execution execution) {
        LOG.debug("Removing execution '{}' for '{}'.",
                execution.getIri(), execution.getPipeline());
        executionFacade.deleteExecution(execution);
    }

    @Override
    public void onExecutionHasFinalData(Execution execution) {
        pruneHistory(execution.getPipeline());
    }

    private void pruneHistory(Resource pipeline) {
        List<Execution> executions = executionFacade.getExecutions().stream()
                .filter(exec -> pipeline.equals(exec.getPipeline()))
                .filter(exec -> !ignoredStates.contains(exec.getStatus()))
                .sorted(Comparator.comparing(Execution::getLastOverviewChange)
                        .reversed())
                .toList();
        for (int index = historyLimit; index < executions.size(); ++index) {
            deleteExecution(executions.get(index));
        }
    }

}
