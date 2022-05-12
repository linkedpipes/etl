package com.linkedpipes.etl.executor.monitor.events;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This component start a copy of a DANGLING pipeline.
 */
class ReExecutor implements EventListener {

    private static final Logger LOG =
            LoggerFactory.getLogger(ReExecutor.class);

    private final Integer retryCount;

    private ExecutionFacade executionFacade = null;

    public ReExecutor(Integer retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public void onExecutionStatusDidChange(
            Execution execution, ExecutionStatus oldStatus) {
        // Ignore.
    }

    @Override
    public void onExecutionHasFinalData(Execution execution) {
        processExecution(execution);
    }

    private void processExecution(Execution execution) {
        if (executionFacade == null) {
            LOG.error("Execution is not ready!");
        }
        if (execution.getStatus() != ExecutionStatus.DANGLING) {
            return;
        }
        if (!shouldReExecute(execution)) {
            return;
        }
        reExecute(execution);
    }

    private boolean shouldReExecute(Execution execution) {
        Resource pipeline = execution.getPipeline();
        List<Execution> executions = executionFacade.getExecutions().stream()
                .filter(exec -> pipeline.equals(exec.getPipeline()))
                .sorted(Comparator.comparing(Execution::getLastOverviewChange)
                        .reversed())
                .toList();
        if (executions.isEmpty()) {
            // This should not happen as we should always have at least
            // the execution that has been finished. Yet if so, we just
            // allow the re-execution.
            return true;
        }
        // It is possible that there is a new execution of the same pipeline.
        Execution newest = executions.get(0);
        if (!ExecutionStatus.DANGLING.equals(newest.getStatus())) {
            // Newest execution of given pipeline is not danling.
            return false;
        }
        int danglingCount = 0;
        // Search how many dangling executions there are for this pipeline.
        for (Execution item : executions) {
            if (!ExecutionStatus.DANGLING.equals(item.getStatus())) {
                // Stop counting when we get to any non-dangling.
                break;
            }
            danglingCount++;
        }
        LOG.info("ReExecutor dangling: {} limit: {} for {}",
                danglingCount, retryCount, execution.getId());
        return retryCount == null || danglingCount < retryCount;
    }

    private void reExecute(Execution execution) {
        try {
            executionFacade.cloneAsNewExecution(execution);
        } catch (MonitorException ex) {
            LOG.error("Can't re-execute execution: {}",
                    execution.getId(), ex);
        }
        LOG.info("Re-executing execution: {}", execution.getId());
    }

    @Override
    public void onExecutionFacadeReady(ExecutionFacade executions) {
        this.executionFacade = executions;
        onExecutionsLoaded();
    }

    public void onExecutionsLoaded() {
        // Collect latest executions for all pipelines.
        Map<Resource, Execution> candidates = new HashMap<>();
        List<Execution> executions = executionFacade.getExecutions().stream()
                .sorted(Comparator.comparing(Execution::getLastOverviewChange)
                        .reversed())
                .toList();
        for (Execution execution : executions) {
            Resource pipeline = execution.getPipeline();
            if (candidates.containsKey(pipeline)) {
                continue;
            }
            candidates.put(execution.getPipeline(), execution);
        }
        // Check them for re-execution.
        for (Execution execution : candidates.values()) {
            processExecution(execution);
        }
    }

}
