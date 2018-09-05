package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.executor.Executor;
import com.linkedpipes.etl.executor.monitor.executor.ExecutorEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class EventListener implements ExecutorEventListener {

    private static final Logger LOG =
            LoggerFactory.getLogger(EventListener.class);

    private final ExecutionStorage storage;

    @Autowired
    public EventListener(ExecutionStorage storage) {
        this.storage = storage;
    }

    public void onAttachExecutor(Execution execution, Executor executor) {
        // Short after this there should be an update coming, so we do not
        // need to check for anything.
        execution.setExecutor(executor);
    }

    public void onUnresponsiveExecutor(Execution execution) {
        StatusSetter.setStatus(execution, ExecutionStatus.UNRESPONSIVE);
    }

    public void onDetachExecutor(Execution execution) {
        LOG.info("onDetachExecutor ...");
        execution.setExecutor(null);
        this.storage.update(execution);
        LOG.info("onDetachExecutor ... done");
    }

    @Override
    public void onOverview(Execution execution, JsonNode overview) {
        this.storage.updateOverview(execution, overview);
    }

}
