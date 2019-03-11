package com.linkedpipes.etl.executor.monitor.events;

import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
class EventListenerAggregator implements EventListener {

    private final List<EventListener> listeners = new ArrayList<>();

    @Autowired
    public EventListenerAggregator(Configuration configuration) {
        String slackUrl = configuration.getSlackWebhook();
        String localUrl = configuration.getLocalUrl();
        if (slackUrl != null) {
            listeners.add(new SlackNotification(slackUrl, localUrl));
        }
    }

    @Override
    public void onExecutionStatusDidChange(
            Execution execution, ExecutionStatus oldStatus) {
        listeners.forEach(listener ->
                listener.onExecutionStatusDidChange(execution, oldStatus));
    }

    @Override
    public void onExecutionHasFinalData(Execution execution) {
        listeners.forEach(listener ->
                listener.onExecutionHasFinalData(execution));
    }

}
