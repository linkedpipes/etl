package com.linkedpipes.etl.executor.monitor.execution.overview;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Java representation of an overview JSON.
 * Designed to obtain needed information from JSON not to be a comprehensive
 * representation.
 */
public class OverviewObject {

    private static final DateFormat DATE_FORMAT = new
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private static final Logger LOG =
            LoggerFactory.getLogger(OverviewObject.class);

    private String pipeline;

    private Integer progressCurrent;

    private Integer progressTotal;

    private Date start;

    private Date finish;

    private String status;

    private Date lastChange;

    public static OverviewObject fromJson(JsonNode root) {
        OverviewObject overview = new OverviewObject();

        overview.status = root.get("status").get("@id").asText();
        overview.lastChange = asDate(root.get("lastChange").asText());

        if (root.get("pipeline") != null) {
            JsonNode id = root.get("pipeline").get("@id");
            if (id != null) {
                overview.pipeline = id.asText();
            }
        }

        if (root.get("executionStarted") != null) {
            overview.start = asDate(root.get("executionStarted").asText());
        }

        if (root.get("executionFinished") != null) {
            overview.finish = asDate(root.get("executionFinished").asText());
        }

        JsonNode progress = root.get("pipelineProgress");
        if (root.get("pipelineProgress") != null) {
            overview.progressCurrent = progress.get("current").asInt();
            overview.progressTotal = progress.get("total").asInt();
        }

        return overview;
    }

    private static Date asDate(String str) {
        if (str == null) {
            return null;
        }

        try {
            return DATE_FORMAT.parse(str);
        } catch(ParseException ex) {
            LOG.info("Can not parse date from overview: ", str);
            return null;
        }
    }

    public static String getIri(JsonNode root) {
        return root.get("execution").get("@id").asText();
    }

    public String getPipeline() {
        return pipeline;
    }

    public Integer getProgressCurrent() {
        return progressCurrent;
    }

    public Integer getProgressTotal() {
        return progressTotal;
    }

    public Date getStart() {
        return start;
    }

    public Date getFinish() {
        return finish;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastChange() {
        return lastChange;
    }

}
