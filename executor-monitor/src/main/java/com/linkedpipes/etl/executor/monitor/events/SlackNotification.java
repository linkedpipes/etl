package com.linkedpipes.etl.executor.monitor.events;

import com.linkedpipes.etl.executor.monitor.execution.Execution;
import com.linkedpipes.etl.executor.monitor.execution.ExecutionStatus;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

class SlackNotification implements EventListener {

    private static final Logger LOG =
            LoggerFactory.getLogger(SlackNotification.class);

    private final String slackUrl;

    public SlackNotification(String slackUrl) {
        this.slackUrl = slackUrl;
    }

    @Override
    public void onExecutionStatusDidChange(
            Execution execution, ExecutionStatus oldStatus) {
        switch (execution.getStatus()) {
            case DANGLING:
                onExecutionDangling(execution);
            default:
                break;
        }
    }

    private void onExecutionDangling(Execution execution) {
        String message = createMessage(
                "Pipeline lost executor.",
                "#f44242",
                getPipelineName(execution), execution.getIri());
        sendMessage(message);
    }

    private String createMessage(
            String message, String color, String pipelineName,
            String executionIri) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"attachments\": [ {");

        builder.append("\"fallback\": \"");
        builder.append(message);
        builder.append("\",");

        builder.append("\"color\": \"");
        builder.append(color);
        builder.append("\",");

        builder.append("\"pretext\": \"");
        builder.append(message);
        builder.append("\",");

        builder.append("\"title\": \"");
        builder.append(pipelineName);
        builder.append("\",");

        builder.append("\"title_link\": \"");
        builder.append(executionIri);
        builder.append("\",");

        builder.append("} ] }");
        return builder.toString();
    }

    private String getPipelineName(Execution execution) {
        for (Statement statement : execution.getPipelineStatements()) {
            if (statement.getSubject() != execution.getPipeline()) {
                continue;
            }
            if (SKOS.PREF_LABEL.equals(statement.getPredicate())) {
                return statement.getObject().stringValue();
            }
        }
        return "[unknown]";
    }

    private void sendMessage(String message) {
        try {
            URL url = new URL(slackUrl);
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.addRequestProperty(
                    "Content-Type", "application/json");
            connection.setRequestProperty(
                    "Content-Length", Integer.toString(message.length()));
            connection.getOutputStream().write(message.getBytes("UTF8"));
            // Get response code - also make the connection happen.
            int responseCode = connection.getResponseCode();
            LOG.debug("Slack response: {}", responseCode);
        } catch (Throwable ex) {
            LOG.error("Can't send message to Slack.", ex);
        }
    }

    @Override
    public void onExecutionHasFinalData(Execution execution) {
        switch (execution.getStatus()) {
            case FINISHED:
                onExecutionFinished(execution);
                break;
            case FAILED:
                onExecutionFailed(execution);
                break;
            case CANCELLED:
                onExecutionCancelled(execution);
                break;
            default:
                break;
        }
    }

    private void onExecutionFinished(Execution execution) {
        String message = createMessage(
                "Pipeline execution finished.",
                "#2b8727",
                getPipelineName(execution), execution.getIri());
        sendMessage(message);
    }

    private void onExecutionFailed(Execution execution) {
        String message = createMessage(
                "Pipeline execution failed.",
                "#f44242",
                getPipelineName(execution), execution.getIri());
        sendMessage(message);
    }

    private void onExecutionCancelled(Execution execution) {
        String message = createMessage(
                "Pipeline execution cancelled.",
                "#dd9a3b",
                getPipelineName(execution), execution.getIri());
        sendMessage(message);
    }


}
