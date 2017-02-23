package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.vocabulary.XSD;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Copy methods from {@link Execution} and provide output compatible
 * with version 1.
 */
class ExecutionModelV1 {

    private enum Status {
        MAPPED("http://etl.linkedpipes.com/resources/status/mapped"),
        QUEUED("http://etl.linkedpipes.com/resources/status/queued"),
        INITIALIZING(
                "http://etl.linkedpipes.com/resources/status/initializing"),
        RUNNING("http://etl.linkedpipes.com/resources/status/running"),
        FINISHED("http://etl.linkedpipes.com/resources/status/finished"),
        CANCELLED("http://etl.linkedpipes.com/resources/status/cancelled"),
        CANCELLING("http://etl.linkedpipes.com/resources/status/cancelling"),
        FAILED("http://etl.linkedpipes.com/resources/status/failed");

        private final String iri;

        Status(String iri) {
            this.iri = iri;
        }

        public String getIri() {
            return iri;
        }
    }

    private final static DateFormat DATE_FORMAT
            = new SimpleDateFormat("YYYY-MM-dd");

    private final static DateFormat TIME_FORMAT
            = new SimpleDateFormat("HH:mm:ss.SSS");

    public static final String LP_PREFIX = "http://linkedpipes.com/ontology/";

    public static final String ETL_PREFIX =
            "http://etl.linkedpipes.com/ontology/";

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecutionModelV1.class);

    private final IRI executionIri;

    private final ResourceManager resourceManager;

    private List<Statement> pipelineStatements = Collections.EMPTY_LIST;

    private final List<List<Statement>> eventsStatements = new ArrayList<>();

    private final ValueFactory vf = SimpleValueFactory.getInstance();

    private Status status = Status.INITIALIZING;

    private Date lastChange = new Date();

    private int eventCounter = 0;

    private final Map<String, Status> componentStatus = new HashMap<>();

    private boolean pipelineFailed = false;

    private boolean pipelineCancelled = false;

    public ExecutionModelV1(String iri, ResourceManager resourceManager) {
        this.executionIri = vf.createIRI(iri);
        this.resourceManager = resourceManager;
        //
        createExecutionRecord();
    }

    protected void createExecutionRecord() {
        status = Status.RUNNING;
        final List<Statement> statements = new ArrayList<>(1);
        statements.add(vf.createStatement(executionIri, RDF.TYPE,
                vf.createIRI(ETL_PREFIX + "Execution"),
                executionIri));
        pipelineStatements = statements;
    }

    public void bindToPipeline(PipelineModel pipeline) {
        final List<Statement> statements = new ArrayList<>(1024);
        statements.addAll(pipelineStatements);

        pipelineStatements.add(vf.createStatement(executionIri,
                vf.createIRI(ETL_PREFIX + "pipeline"),
                vf.createIRI(pipeline.getIri()),
                executionIri));
        for (PipelineModel.Component component : pipeline.getComponents()) {
            if (component.getExecutionType() ==
                    PipelineModel.ExecutionType.SKIP) {
                continue;
            }
            final IRI componentIri = vf.createIRI(component.getIri());
            statements.add(vf.createStatement(executionIri,
                    vf.createIRI(LP_PREFIX + "component"),
                    componentIri, executionIri));
            statements.add(vf.createStatement(componentIri,
                    RDF.TYPE, vf.createIRI(LP_PIPELINE.COMPONENT),
                    executionIri));
            statements.add(vf.createStatement(componentIri,
                    vf.createIRI(LP_PREFIX + "order"),
                    vf.createLiteral(component.getOrder()),
                    executionIri));
            for (PipelineModel.DataUnit dataUnit : component.getDataUnits()) {
                final IRI dataUnitIri = vf.createIRI(dataUnit.getIri());
                statements
                        .add(vf.createStatement(componentIri,
                                vf.createIRI(ETL_PREFIX + "dataUnit"),
                                dataUnitIri, executionIri));
                statements.add(vf.createStatement(dataUnitIri, RDF.TYPE,
                        vf.createIRI(ETL_PREFIX + "DataUnit"),
                        executionIri));
                statements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                        ETL_PREFIX + "binding"),
                        vf.createLiteral(dataUnit.getBinding()),
                        executionIri));
                //
                final PipelineModel.DataSource source =
                        dataUnit.getDataSource();
                if (source != null) {
                    statements.add(vf.createStatement(dataUnitIri,
                            vf.createIRI(LP_EXEC.HAS_EXECUTION),
                            vf.createIRI(source.getExecution()),
                            executionIri));
                    statements.add(vf.createStatement(dataUnitIri,
                            vf.createIRI(LP_EXEC.HAS_LOAD_PATH),
                            vf.createLiteral(source.getLoadPath()),
                            executionIri));
                }
            }
            //
            componentStatus.put(component.getIri(), Status.QUEUED);
        }
        pipelineStatements = statements;
        lastChange = new Date();
    }

    public void onEvent(ExecutionModel.Component component, Event event) {
        final int index = ++eventCounter;
        final IRI eventIri = createEventIri(index);
        final List<Statement> eventStatements =
                createBaseEvent(eventIri, index, null);

        eventStatements.add(vf.createStatement(eventIri,
                vf.createIRI("http://linkedpipes.com/ontology/component"),
                vf.createIRI(component.getComponentIri()),
                executionIri));

        event.setIri(eventIri.stringValue());

        event.write(new RdfSource.TripleWriter() {

            @Override
            public void iri(String s, String p, String o) {
                eventStatements.add(vf.createStatement(vf.createIRI(s),
                        vf.createIRI(p), vf.createIRI(o), executionIri));
            }

            @Override
            public void typed(String s, String p, String o, String type) {
                eventStatements.add(vf.createStatement(vf.createIRI(s),
                        vf.createIRI(p),
                        vf.createLiteral(o, vf.createIRI(type)),
                        executionIri));
            }

            @Override
            public void string(String s, String p, String o, String language) {
                if (language == null) {
                    eventStatements.add(vf.createStatement(vf.createIRI(s),
                            vf.createIRI(p), vf.createLiteral(o),
                            executionIri));
                } else {
                    eventStatements.add(vf.createStatement(vf.createIRI(s),
                            vf.createIRI(p),
                            vf.createLiteral(o, language), executionIri));
                }
            }

            @Override
            public void submit() throws RdfUtilsException {
                // Do nothing here.
            }

        });
        eventsStatements.add(eventStatements);
        lastChange = new Date();
        writeToDisk();
    }

    public void onExecutionCancelled() {
        pipelineCancelled = true;
        lastChange = new Date();
        status = Status.CANCELLING;
        writeToDisk();
    }

    public void onExecutionFailed() {
        pipelineFailed = true;
        lastChange = new Date();
        writeToDisk();
    }

    public void onComponentBegin(ExecutionModel.Component component) {
        final int index = ++eventCounter;
        final IRI eventIri = createEventIri(index);
        final List<Statement> eventStatements =
                createBaseEvent(eventIri, index, "Execution started.");
        //
        eventStatements.add(vf.createStatement(eventIri, RDF.TYPE,
                vf.createIRI(LP_PREFIX + "events/ComponentBegin"),
                executionIri));
        eventStatements.add(vf.createStatement(eventIri,
                vf.createIRI("http://linkedpipes.com/ontology/component"),
                vf.createIRI(component.getComponentIri()),
                executionIri));
        // Add information about data units.
        for (ExecutionModel.DataUnit dataUnit : component.getDataUnits()) {
            final IRI dataUnitIri = vf.createIRI(dataUnit.getDataUnitIri());
            eventStatements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/dataPath"),
                    vf.createLiteral(dataUnit.getRelativeDataPath()),
                    executionIri));
            eventStatements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/debug"),
                    vf.createLiteral(dataUnit.getVirtualDebugPath()),
                    executionIri));
        }
        //
        eventsStatements.add(eventStatements);
        componentStatus.put(component.getComponentIri(), Status.RUNNING);
        lastChange = new Date();
        writeToDisk();
    }

    public void onComponentEnd(ExecutionModel.Component component) {
        final int index = ++eventCounter;
        final IRI eventIri = createEventIri(index);
        final List<Statement> eventStatements =
                createBaseEvent(eventIri, index, "Execution completed.");
        //
        eventStatements.add(vf.createStatement(eventIri, RDF.TYPE, vf.createIRI(
                "http://linkedpipes.com/ontology/events/ComponentEnd"),
                executionIri));
        eventStatements.add(vf.createStatement(eventIri,
                vf.createIRI("http://linkedpipes.com/ontology/component"),
                vf.createIRI(component.getComponentIri()),
                executionIri));
        //
        eventsStatements.add(eventStatements);
        componentStatus.put(component.getComponentIri(), Status.FINISHED);
        lastChange = new Date();
        writeToDisk();
    }

    public void onComponentMapped(ExecutionModel.Component component) {
        componentStatus.put(component.getComponentIri(), Status.MAPPED);
        for (ExecutionModel.DataUnit dataUnit : component.getDataUnits()) {
            final IRI dataUnitIri = vf.createIRI(dataUnit.getDataUnitIri());
            pipelineStatements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/dataPath"),
                    vf.createLiteral(dataUnit.getRelativeDataPath()),
                    executionIri));
            pipelineStatements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/debug"),
                    vf.createLiteral(dataUnit.getVirtualDebugPath()),
                    executionIri));
        }
        lastChange = new Date();
        writeToDisk();
    }

    public void onComponentFailed(ExecutionModel.Component component,
            LpException exception) {
        //
        final int index = ++eventCounter;
        final IRI eventIri = createEventIri(index);
        final List<Statement> eventStatements =
                createBaseEvent(eventIri, index, "Execution failed.");
        //
        eventStatements.add(vf.createStatement(eventIri, RDF.TYPE, vf.createIRI(
                LP_PREFIX + "events/ComponentFailed"),
                executionIri));
        eventStatements.add(vf.createStatement(eventIri,
                vf.createIRI("http://linkedpipes.com/ontology/component"),
                vf.createIRI(component.getComponentIri()),
                executionIri));
        // Get exceptions.
        LpException lpException = null;
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            if (lpException == null && rootCause instanceof LpException) {
                lpException = (LpException) rootCause;
            }
            rootCause = rootCause.getCause();
        }
        // Format into a message.
        if (lpException != null) {
            eventStatements.add(vf.createStatement(eventIri,
                    vf.createIRI(LP_PREFIX + "events/reason"),
                    vf.createLiteral(lpException.getMessage()),
                    executionIri));
        }
        if (rootCause.getMessage() == null) {
            eventStatements.add(vf.createStatement(eventIri,
                    vf.createIRI(LP_PREFIX + "events/rootException"),
                    vf.createLiteral(rootCause.getClass().getSimpleName()),
                    executionIri));
        } else {
            final String message = rootCause.getClass().getSimpleName() +
                    " : " + rootCause.getMessage();
            eventStatements.add(vf.createStatement(eventIri,
                    vf.createIRI(LP_PREFIX + "events/rootException"),
                    vf.createLiteral(message),
                    executionIri));
        }
        //
        eventsStatements.add(eventStatements);
        componentStatus.put(component.getComponentIri(), Status.FAILED);
        pipelineFailed = true;
        lastChange = new Date();
        writeToDisk();
    }

    public void onExecutionBegin() {
        final int index = ++eventCounter;
        final IRI eventIri = createEventIri(index);
        final List<Statement> eventStatements =
                createBaseEvent(eventIri, index, "Execution begin.");
        //
        eventStatements.add(vf.createStatement(eventIri, RDF.TYPE,
                vf.createIRI(LP_PREFIX + "events/ExecutionBegin"),
                executionIri));
        //
        eventsStatements.add(eventStatements);
        lastChange = new Date();
        writeToDisk();
    }

    public void onExecutionEnd() {
        final int index = ++eventCounter;
        final IRI eventIri = createEventIri(index);
        final List<Statement> eventStatements =
                createBaseEvent(eventIri, index, "Execution finished.");
        //
        eventStatements.add(vf.createStatement(eventIri, RDF.TYPE,
                vf.createIRI(LP_PREFIX + "events/ExecutionEnd"),
                executionIri));
        //
        eventsStatements.add(eventStatements);
        if (pipelineFailed) {
            status = Status.FAILED;
        } else if (pipelineCancelled) {
            status = Status.CANCELLED;
        } else {
            status = Status.FINISHED;
        }
        lastChange = new Date();
        writeToDisk();
    }

    public void writeToDisk() {
        final File outputFile = resourceManager.getExecutionFileV1();
        try (final OutputStream stream = new FileOutputStream(outputFile)) {
            writeToDisk(stream, RDFFormat.JSONLD);
        } catch (IOException | ExecutorException ex) {
            LOG.error("Can't writeToDisk v1 execution.", ex);
        }
        try (final OutputStream stream = new FileOutputStream(
                outputFile.toString().replace("jsonld", "trig"))) {
            writeToDisk(stream, RDFFormat.TRIG);
        } catch (IOException | ExecutorException ex) {
            LOG.error("Can't writeToDisk v1 execution.", ex);
        }
    }

    public void writeToDisk(OutputStream stream, RDFFormat format)
            throws ExecutorException {
        try {
            final RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();

            final List<Statement> statementsCopy = new ArrayList(
                    pipelineStatements);
            for (Statement statement : statementsCopy) {
                writer.handleStatement(statement);
            }
            int eventCount = eventsStatements.size();
            for (int i = 0; i < eventCount; ++i) {
                for (Statement statement : eventsStatements.get(i)) {
                    writer.handleStatement(statement);
                }
            }
            for (Statement statement : buildChangingValues()) {
                writer.handleStatement(statement);
            }
            writer.endRDF();
        } catch (RuntimeException ex) {
            throw new ExecutorException("Can't writeToDisk v1Execution.", ex);
        }
    }

    private IRI createEventIri(int index) {
        return vf.createIRI(executionIri.stringValue() + "/events/" +
                Integer.toString(index));
    }

    private List<Statement> createBaseEvent(IRI iri, int order, String label) {
        final List<Statement> eventStatements = new ArrayList<>(16);
        eventStatements.add(vf.createStatement(executionIri,
                vf.createIRI("http://etl.linkdpipes.com/ontology/event"),
                iri, executionIri));
        eventStatements.add(vf.createStatement(iri,
                RDF.TYPE, vf.createIRI(LP_PREFIX + "Event"), executionIri));

        eventStatements.add(vf.createStatement(iri,
                vf.createIRI(LP_PREFIX + "events/created"),
                vf.createLiteral(getNowDateAsString(),
                        vf.createIRI(XSD.DATETIME)), executionIri));

        eventStatements.add(vf.createStatement(iri,
                vf.createIRI(LP_PREFIX + "order"),
                vf.createLiteral(order), executionIri));
        if (label != null) {
            eventStatements.add(vf.createStatement(iri,
                    SKOS.PREF_LABEL, vf.createLiteral(label, "en"),
                    executionIri));
        }
        return eventStatements;
    }

    private static String getNowDateAsString() {
        final Date now = new Date();
        final StringBuilder createdAsString = new StringBuilder(25);
        createdAsString.append(DATE_FORMAT.format(now));
        createdAsString.append("T");
        createdAsString.append(TIME_FORMAT.format(now));
        return createdAsString.toString();
    }

    private List<Statement> buildChangingValues() {
        ArrayList<Statement> statements = new ArrayList<>(2);
        statements.add(vf.createStatement(executionIri,
                vf.createIRI("http://etl.linkedpipes.com/ontology/lastChange"),
                vf.createLiteral(lastChange),
                executionIri));
        statements.add(vf.createStatement(executionIri,
                vf.createIRI("http://etl.linkedpipes.com/ontology/status"),
                vf.createIRI(status.getIri()),
                executionIri));
        for (Map.Entry<String, Status> entry : componentStatus.entrySet()) {
            statements.add(vf.createStatement(
                    vf.createIRI(entry.getKey()),
                    vf.createIRI("http://etl.linkedpipes.com/ontology/status"),
                    vf.createIRI(entry.getValue().getIri()),
                    executionIri));
        }
        return statements;
    }

}
