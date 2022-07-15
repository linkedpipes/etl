package com.linkedpipes.etl.executor.monitor.events;

import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionFacade;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
class EventListenerAggregator implements EventListener {

    private final List<EventListener> listeners = new ArrayList<>();

    private Configuration configuration;

    @PostConstruct
    public void onInit() {
        String slackFinished = configuration.getSlackFinishedWebhook();
        String slackError = configuration.getSlackErrorWebhook();
        if (slackFinished != null || slackError != null) {
            listeners.add(new SlackNotification(
                    slackFinished, slackError,
                    configuration.getLocalUrl()));
        }
        if (configuration.isRetryExecution()) {
            listeners.add(new ReExecutor(
                    configuration.getRetryLimit()));
        }
        if (configuration.getHistoryLimit() != null) {
            listeners.add(new LimitCountHistory(
                    configuration.getHistoryLimit()));
        }
        if (configuration.getHistoryHourLimit() != null) {
            listeners.add(new LimitTimeHistory(
                    configuration.getHistoryHourLimit()));
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

    @Override
    public void onExecutionFacadeReady(ExecutionFacade executions) {
        listeners.forEach(listener ->
                listener.onExecutionFacadeReady(executions));
    }

    @Autowired
    private void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Scheduled(fixedDelay = 60, initialDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void onEveryHour() {
        listeners.forEach(EventListener::onTimeHour);
    }

}
