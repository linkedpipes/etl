package com.linkedpipes.etl.executor.monitor.events;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LimitTimeHistory implements EventListener {

    private static final Logger LOG =
            LoggerFactory.getLogger(LimitTimeHistory.class);

    private final List<ExecutionStatus> ignoredStates = Arrays.asList(
            ExecutionStatus.QUEUED,
            ExecutionStatus.RUNNING,
            ExecutionStatus.CANCELLING,
            ExecutionStatus.UNRESPONSIVE,
            ExecutionStatus.DELETED);

    private final Integer historyHourLimit;

    private final ExecutionFacade executionFacade = null;

    public LimitTimeHistory(Integer historyHourLimit) {
        LOG.info("Using history time limit with limit: {}", historyHourLimit);
        this.historyHourLimit = historyHourLimit;
    }

    @Override
    public void onTimeHour() {
        EventListener.super.onTimeHour();
        pruneOnStartup();
    }


    private void pruneOnStartup() {
        List<Execution> executions = executionFacade.getExecutions().stream()
                .filter(exec -> !ignoredStates.contains(exec.getStatus()))
                .sorted(Comparator.comparing(Execution::getLastOverviewChange)
                        .reversed())
                .toList();
        LocalDateTime now = LocalDateTime.now();
        for (Execution execution : executions) {
            Long diff = ChronoUnit.HOURS.between(
                    now, execution.getLastOverviewChange().toInstant());
            if (diff > historyHourLimit) {
                deleteExecution(execution);
            }
        }
    }

    private void deleteExecution(Execution execution) {
        LOG.debug("Removing execution '{}' for '{}'.",
                execution.getIri(), execution.getPipeline());
        executionFacade.deleteExecution(execution);
    }

}
