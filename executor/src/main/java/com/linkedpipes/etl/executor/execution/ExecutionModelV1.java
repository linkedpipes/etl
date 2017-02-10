package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.event.Event;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
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
import java.util.*;

/**
 * Copy methods from {@link Execution} and provide output compatible
 * with version 1.
 */
class ExecutionModelV1 {

    private enum Status {
        MAPPED("http://etl.linkedpipes.com/resources/status/mapped"),
        INITIALIZING(
                "http://etl.linkedpipes.com/resources/status/initializing"),
        RUNNING("http://etl.linkedpipes.com/resources/status/running"),
        FINISHED("http://etl.linkedpipes.com/resources/status/finished"),
        FAILED("http://etl.linkedpipes.com/resources/status/failed");

        private final String iri;

        Status(String iri) {
            this.iri = iri;
        }

        public String getIri() {
            return iri;
        }
    }

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecutionModelV1.class);

    private final IRI iri;

    private final ResourceManager resourceManager;

    private final ArrayList<Statement> statements = new ArrayList<>(1024);

    private final ValueFactory vf = SimpleValueFactory.getInstance();

    private Status status = Status.INITIALIZING;

    private Date lastChange = new Date();

    private int eventCounter = 0;

    private final Map<String, Status> componentStatus = new HashMap<>();

    private boolean pipelineFailed = false;

    public ExecutionModelV1(String iri, ResourceManager resourceManager) {
        this.iri = vf.createIRI(iri);
        this.resourceManager = resourceManager;
        //
        createExecutionRecord();
    }

    protected void createExecutionRecord() {
        status = Status.RUNNING;
        statements.add(vf.createStatement(iri, RDF.TYPE,
                vf.createIRI("http://etl.linkedpipes.com/ontology/Execution"),
                iri));
    }

    public void bindToPipeline(PipelineModel pipeline) {
        statements.add(vf.createStatement(iri,
                vf.createIRI("http://etl.linkedpipes.com/ontology/pipeline"),
                vf.createIRI(pipeline.getIri()),
                iri));
        for (PipelineModel.Component component : pipeline.getComponents()) {
            if (component.getExecutionType() ==
                    PipelineModel.ExecutionType.SKIP) {
                continue;
            }
            final IRI componentIri = vf.createIRI(component.getIri());
            statements.add(vf.createStatement(iri,
                    vf.createIRI("http://linkedpipes.com/ontology/component"),
                    componentIri,
                    iri));
            statements.add(vf.createStatement(componentIri,
                    RDF.TYPE,
                    vf.createIRI(LP_PIPELINE.COMPONENT),
                    iri));
            statements.add(vf.createStatement(componentIri,
                    vf.createIRI("http://linkedpipes.com/ontology/order"),
                    vf.createLiteral(component.getOrder()),
                    iri));
            for (PipelineModel.DataUnit dataUnit : component.getDataUnits()) {
                final IRI dataUnitIri = vf.createIRI(dataUnit.getIri());
                statements.add(vf.createStatement(componentIri, vf.createIRI(
                        "http://etl.linkedpipes.com/ontology/dataUnit"),
                        dataUnitIri,
                        iri));
                statements.add(vf.createStatement(dataUnitIri, RDF.TYPE,
                        vf.createIRI(
                                "http://etl.linkedpipes.com/ontology/DataUnit"),
                        iri));
                statements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                        "http://etl.linkedpipes.com/ontology/binding"),
                        vf.createLiteral(dataUnit.getBinding()),
                        iri));
                //
                final PipelineModel.DataSource source =
                        dataUnit.getDataSource();
                if (source != null) {
                    statements.add(vf.createStatement(dataUnitIri,
                            vf.createIRI(LP_EXEC.HAS_EXECUTION),
                            vf.createIRI(source.getExecution()),
                            iri));
                    statements.add(vf.createStatement(dataUnitIri,
                            vf.createIRI(LP_EXEC.HAS_LOAD_PATH),
                            vf.createLiteral(source.getLoadPath()),
                            iri));
                }
            }
        }
        status = Status.RUNNING;
        lastChange = new Date();
    }

    public void onEvent(Execution.Component component, Event event) {
        final IRI eventIri = createBaseEvent(null);
        statements.add(vf.createStatement(eventIri,
                vf.createIRI("http://linkedpipes.com/ontology/component"),
                vf.createIRI(component.getComponentIri()),
                iri));

        event.setIri(eventIri.stringValue());

        event.write(new RdfSource.TripleWriter() {

            @Override
            public void iri(String s, String p, String o) {
                statements.add(vf.createStatement(vf.createIRI(s),
                        vf.createIRI(p), vf.createIRI(o), iri));
            }

            @Override
            public void typed(String s, String p, String o, String type) {
                statements.add(vf.createStatement(vf.createIRI(s),
                        vf.createIRI(p),
                        vf.createLiteral(o, vf.createIRI(type)),
                        iri));
            }

            @Override
            public void string(String s, String p, String o, String language) {
                if (language == null) {
                    statements.add(vf.createStatement(vf.createIRI(s),
                            vf.createIRI(p), vf.createLiteral(o), iri));
                } else {
                    statements.add(vf.createStatement(vf.createIRI(s),
                            vf.createIRI(p),
                            vf.createLiteral(o, language), iri));
                }
            }

            @Override
            public void submit() throws RdfUtilsException {
                // Do nothing here.
            }

        });
        lastChange = new Date();
        write();
    }

    public void onExecutionFailed() {
        pipelineFailed = true;
        lastChange = new Date();
        write();
    }

    public void onComponentBegin(Execution.Component component) {
        final IRI event = createBaseEvent("Execution started.");
        statements.add(vf.createStatement(event, RDF.TYPE, vf.createIRI(
                "http://linkedpipes.com/ontology/events/ComponentBegin"),
                iri));
        statements.add(vf.createStatement(event,
                vf.createIRI("http://linkedpipes.com/ontology/component"),
                vf.createIRI(component.getComponentIri()),
                iri));
        // Add information about data units.
        for (Execution.DataUnit dataUnit : component.getDataUnits()) {
            final IRI dataUnitIri = vf.createIRI(dataUnit.getDataUnitIri());
            statements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/dataPath"),
                    vf.createLiteral(dataUnit.getRelativeDataPath()),
                    iri));
            statements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/debug"),
                    vf.createLiteral(dataUnit.getVirtualDebugPath()),
                    iri));
        }
        //
        componentStatus.put(component.getComponentIri(), Status.RUNNING);
        lastChange = new Date();
        write();
    }

    public void onComponentEnd(Execution.Component component) {
        final IRI event = createBaseEvent("Execution completed.");
        statements.add(vf.createStatement(event, RDF.TYPE, vf.createIRI(
                "http://linkedpipes.com/ontology/events/ComponentEnd"),
                iri));
        statements.add(vf.createStatement(event,
                vf.createIRI("http://linkedpipes.com/ontology/component"),
                vf.createIRI(component.getComponentIri()),
                iri));
        //
        componentStatus.put(component.getComponentIri(), Status.FINISHED);
        lastChange = new Date();
        write();
    }

    public void onComponentMapped(Execution.Component component) {
        componentStatus.put(component.getComponentIri(), Status.MAPPED);
        for (Execution.DataUnit dataUnit : component.getDataUnits()) {
            final IRI dataUnitIri = vf.createIRI(dataUnit.getDataUnitIri());
            statements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/dataPath"),
                    vf.createLiteral(dataUnit.getRelativeDataPath()),
                    iri));
            statements.add(vf.createStatement(dataUnitIri, vf.createIRI(
                    "http://etl.linkedpipes.com/ontology/debug"),
                    vf.createLiteral(dataUnit.getVirtualDebugPath()),
                    iri));
        }
        lastChange = new Date();
        write();
    }

    public void onComponentFailed(Execution.Component component,
            LpException exception) {
        componentStatus.put(component.getComponentIri(), Status.FAILED);
        pipelineFailed = true;
        lastChange = new Date();
        write();
    }

    public void onExecutionBegin() {
        final IRI event = createBaseEvent("Execution started.");
        statements.add(vf.createStatement(event, RDF.TYPE, vf.createIRI(
                "http://linkedpipes.com/ontology/events/ExecutionBegin"),
                iri));
        lastChange = new Date();
        write();
    }

    public void onExecutionEnd() {
        final IRI event = createBaseEvent("Execution finished.");
        statements.add(vf.createStatement(event, RDF.TYPE, vf.createIRI(
                "http://linkedpipes.com/ontology/events/ExecutionEnd"),
                iri));
        if (pipelineFailed) {
            status = Status.FAILED;
        } else {
            status = Status.FINISHED;
        }
        lastChange = new Date();
        write();
    }

    public void write() {
        final File outputFile = resourceManager.getExecutionFileV1();
        try (final OutputStream stream = new FileOutputStream(outputFile)) {
            write(stream, RDFFormat.JSONLD);
        } catch (IOException | ExecutorException ex) {
            LOG.error("Can't write v1 execution.", ex);
        }
        try (final OutputStream stream = new FileOutputStream(
                outputFile.toString().replace("jsonld", "trig"))) {
            write(stream, RDFFormat.TRIG);
        } catch (IOException | ExecutorException ex) {
            LOG.error("Can't write v1 execution.", ex);
        }
    }

    public void write(OutputStream stream, RDFFormat format)
            throws ExecutorException {
        try {
            final RDFWriter writer = Rio.createWriter(format, stream);
            writer.startRDF();
            for (final Statement statement : statements) {
                writer.handleStatement(statement);
            }
            for (final Statement statement : changingValues()) {
                writer.handleStatement(statement);
            }
            writer.endRDF();
        } catch (RuntimeException ex) {
            throw new ExecutorException("Can't write v1Execution.", ex);
        }
    }

    private IRI createBaseEvent(String label) {
        int eventIndex = ++eventCounter;
        final IRI eventIri = vf.createIRI(iri.stringValue() + "/events/" +
                Integer.toString(eventIndex));
        statements.add(vf.createStatement(iri,
                vf.createIRI("http://etl.linkdpipes.com/ontology/event"),
                eventIri,
                iri));
        //
        statements.add(vf.createStatement(eventIri,
                RDF.TYPE,
                vf.createIRI("http://linkedpipes.com/ontology/Event"),
                iri));
        statements.add(vf.createStatement(eventIri,
                vf.createIRI("http://linkedpipes.com/ontology/events/created"),
                vf.createLiteral(new Date()),
                iri));
        statements.add(vf.createStatement(eventIri,
                vf.createIRI("http://linkedpipes.com/ontology/order"),
                vf.createLiteral(eventIndex),
                iri));
        if (label != null) {
            statements.add(vf.createStatement(eventIri,
                    SKOS.PREF_LABEL,
                    vf.createLiteral(label, "en"),
                    iri));
        }
        return eventIri;
    }

    /**
     * @return Generated (updated) statements.
     */
    private List<Statement> changingValues() {
        ArrayList<Statement> statements = new ArrayList<>(2);
        statements.add(vf.createStatement(iri,
                vf.createIRI("http://etl.linkedpipes.com/ontology/lastChange"),
                vf.createLiteral(lastChange),
                iri));
        statements.add(vf.createStatement(iri,
                vf.createIRI("http://etl.linkedpipes.com/ontology/status"),
                vf.createIRI(status.getIri()),
                iri));
        for (Map.Entry<String, Status> entry : componentStatus.entrySet()) {
            statements.add(vf.createStatement(
                    vf.createIRI(entry.getKey()),
                    vf.createIRI("http://etl.linkedpipes.com/ontology/status"),
                    vf.createIRI(entry.getValue().getIri()),
                    iri));
        }
        return statements;
    }

}
