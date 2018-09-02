package com.linkedpipes.etl.executor.monitor.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_OVERVIEW;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.rdf4j.Statements;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class can be used only from a single thread.
 */
public class OverviewLoader {

    private static final Logger LOG =
            LoggerFactory.getLogger(OverviewLoader.class);

    public static final String DATETIME_TYPE =
            "http://www.w3.org/2001/XMLSchema#dateTime";

    private final DateFormat dateFormat = new
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final ObjectMapper mapper = new ObjectMapper();

    private final MonitorStatements monitorStatements =
            new MonitorStatements();

    private Date checkStart;

    public void loadFromDirectory(Execution execution) throws MonitorException {
        this.checkStart = new Date();
        File file = this.getOverviewFile(execution);
        if (!file.exists()) {
            this.handleQueued(execution);
            return;
        }
        try (InputStream stream = new FileInputStream(file)) {
            this.loadFromStream(execution, stream);
        } catch (IOException ex) {
            throw new MonitorException("", ex);
        }
    }

    private File getOverviewFile(Execution execution) {
        return new File(execution.getDirectory(), "execution-overview.jsonld");
    }

    private void handleQueued(Execution execution) {
        this.createQueuedOverview(execution);
        this.updateExecutionFromOverview(execution);
    }

    private Value getDirSizeValue(Execution execution) {
        return this.valueFactory.createLiteral(
                FileUtils.sizeOfDirectory(execution.getDirectory()));
    }

    private void createQueuedOverview(Execution execution) {
        ObjectNode contextNode = this.mapper.createObjectNode();
        contextNode.put("execution", LP_OVERVIEW.HAS_EXECUTION);
        contextNode.put("status", LP_OVERVIEW.HAS_STATUS);
        contextNode.put("pipeline", LP_OVERVIEW.HAS_PIPELINE);

        ObjectNode lastChangNode = mapper.createObjectNode();
        lastChangNode.put("@id", LP_OVERVIEW.HAS_LAST_CHANGE);
        lastChangNode.put("@type", DATETIME_TYPE);
        contextNode.set("lastChange", lastChangNode);

        //

        ObjectNode rootNode = this.mapper.createObjectNode();
        rootNode.set("@context", contextNode);
        rootNode.put("@id", execution.getIri() + "/overview");

        ObjectNode executionNode = this.mapper.createObjectNode();
        executionNode.put("@id", execution.getIri());
        rootNode.set("execution", executionNode);

        ObjectNode statusNode = this.mapper.createObjectNode();
        statusNode.put("@id", LP_EXEC.STATUS_QUEUED);
        rootNode.set("status", statusNode);

        ObjectNode pipelineNode = this.mapper.createObjectNode();
        pipelineNode.put("@id", execution.getPipeline().stringValue());
        rootNode.set("pipeline", pipelineNode);

        String lastChange = this.dateFormat.format(new Date());
        rootNode.put("lastChange", lastChange);

        execution.setOverview(rootNode);
    }

    public void loadFromStream(Execution execution, InputStream stream)
            throws MonitorException {
        this.checkStart = new Date();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(stream);
        } catch (IOException ex) {
            throw new MonitorException("Can't read overview JSON.", ex);
        }
        execution.setOverview(rootNode);
        if (!execution.hasExecutor()) {
            this.handleMissingExecutor(execution);
        }
        this.updateExecutionFromOverview(execution);
    }

    private void updateExecutionFromOverview(Execution execution) {
        execution.setLastCheck(this.checkStart);

        JsonNode root = execution.getOverview();
        try {
            Date date = this.dateFormat.parse(root.get("lastChange").asText());
            execution.setLastExecutionChange(date);
        } catch (ParseException ex) {
            LOG.info("Can not parse last change date");
        }

        String statusIriAsStr = root.get("status").get("@id").asText();
        execution.setStatus(ExecutionStatus.fromIri(statusIriAsStr));

        this.updateStatementsFromOverview(execution);
    }

    private void updateStatementsFromOverview(Execution execution) {
        JsonNode root = execution.getOverview();

        IRI overviewIri = this.valueFactory.createIRI(execution.getIri());
        Statements statements = Statements.ArrayList();
        statements.setDefaultGraph(execution.getListGraph());

        statements.addIri(
                overviewIri,
                RDF.TYPE,
                LP_EXEC.EXECUTION);

        statements.addIri(
                overviewIri,
                LP_OVERVIEW.HAS_PIPELINE,
                root.get("pipeline").get("@id").asText());

        if (root.get("pipelineProgress") != null) {
            statements.addInt(
                    overviewIri,
                    LP_OVERVIEW.HAS_PROGRESS_TOTAL,
                    root.get("pipelineProgress").get("total").asInt());
            statements.addInt(
                    overviewIri,
                    LP_OVERVIEW.HAS_PROGRESS_CURRENT,
                    root.get("pipelineProgress").get("current").asInt());
        }

        if (root.get("executionStarted") != null) {
            statements.add(
                    overviewIri,
                    LP_OVERVIEW.HAS_START,
                    this.createDate(root.get("executionStarted").asText()));
        }

        if (root.get("executionFinished") != null) {
            statements.add(
                    overviewIri,
                    LP_OVERVIEW.HAS_END,
                    this.createDate(root.get("executionFinished").asText()));
        }

        statements.addIri(
                overviewIri,
                LP_OVERVIEW.HAS_STATUS,
                root.get("status").get("@id").asText());

        statements.add(
                overviewIri,
                LP_EXEC.HAS_SIZE,
                this.getDirSizeValue(execution));

        this.monitorStatements.update(execution);

        execution.setOverviewStatements(statements);
    }

    private Value createDate(String value) {
        String type = "http://www.w3.org/2001/XMLSchema#dateTime";
        return this.valueFactory.createLiteral(value,
                this.valueFactory.createIRI(type));
    }

    private void handleMissingExecutor(Execution execution) {
        ObjectNode root = (ObjectNode) execution.getOverview();
        switch (root.get("status").asText()) {
            case LP_EXEC.STATUS_RUNNING:
            case LP_EXEC.STATUS_CANCELLING:
                ObjectNode statusNode = mapper.createObjectNode();
                statusNode.put("@id", LP_EXEC.STATUS_UNKNOWN);
                root.set("status", statusNode);
                break;
            default:
                break;
        }
    }

}
