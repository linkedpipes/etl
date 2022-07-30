package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.execution.model.DataUnit;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import com.linkedpipes.etl.executor.pipeline.model.DataSource;
import com.linkedpipes.etl.executor.pipeline.model.ExecutionType;
import com.linkedpipes.etl.executor.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.executor.pipeline.model.PipelineModel;
import com.linkedpipes.etl.executor.pipeline.model.Port;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExecutionInformation {

    // TODO Replace with vocabulary.
    private static final String ETL_PREFIX =
            "http://etl.linkedpipes.com/ontology/";

    // TODO Replace with vocabulary.
    private static final String LP_PREFIX =
            "http://linkedpipes.com/ontology/";

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    /**
     * Holds only statements that do not change over time (like component
     * status).
     */
    private final StatementsBuilder statements =
            Statements.arrayList().builder();

    private IRI iri;

    private ExecutionModel execution;

    private PipelineModel pipeline;

    private final ExecutionStatusMonitor executionStatus;

    private Map<String, String> componentStatus = new HashMap<>();

    private final File file;

    public ExecutionInformation(
            ExecutionStatusMonitor executionStatus,
            ExecutionModel execution,
            File file) {
        this.executionStatus = executionStatus;
        this.execution = execution;
        this.file = file;
        //
        bindToExecution();
    }

    private void bindToExecution() {
        iri = valueFactory.createIRI(execution.getIri());
        statements.setDefaultGraph(iri);
        statements.addIri(iri, RDF.TYPE, ETL_PREFIX + "Execution");
    }

    public void onPipelineLoaded(PipelineModel pipeline) {
        this.pipeline = pipeline;
        statements.add(
                iri,
                LP_EXEC.HAS_DELETE_WORKING_DATA,
                valueFactory.createLiteral(pipeline.isDeleteWorkingData())
        );
        statements.addIri(
                iri, ETL_PREFIX + "pipeline", pipeline.getIri());
        for (PipelineComponent component : pipeline.getComponents()) {
            addPipelineComponent(component);
        }
    }

    private void addPipelineComponent(PipelineComponent component) {
        if (component.getExecutionType() == ExecutionType.SKIP) {
            return;
        }
        IRI componentIri = valueFactory.createIRI(component.getIri());

        statements.add(iri, LP_PREFIX + "component", componentIri);
        statements.addIri(componentIri, RDF.TYPE, LP_PIPELINE.COMPONENT);

        if (component.getExecution() != null) {
            statements.addIri(
                    componentIri,
                    LP_EXEC.HAS_EXECUTION,
                    component.getExecution());
        }

        int order = component.getExecutionOrder();
        statements.add(
                componentIri,
                LP_PREFIX + "order",
                valueFactory.createLiteral(order));

        for (Port port : component.getPorts()) {
            addPipelinePort(componentIri, port);
        }

        componentStatus.put(component.getIri(), LP_EXEC.STATUS_QUEUED);
    }

    private void addPipelinePort(IRI componentIri, Port port) {
        IRI portIri = valueFactory.createIRI(port.getIri());

        statements.add(componentIri, ETL_PREFIX + "dataUnit", portIri);
        statements.addIri(portIri, RDF.TYPE, ETL_PREFIX + "DataUnit");
        statements.add(portIri, ETL_PREFIX + "binding", port.getBinding());

        DataSource source = port.getDataSource();
        if (source != null) {
            addPipelinePortSource(portIri, source);
        }
    }

    private void addPipelinePortSource(IRI portIri, DataSource source) {
        statements.addIri(
                portIri, LP_EXEC.HAS_EXECUTION, source.getExecution());
        statements.add(
                portIri, LP_EXEC.HAS_LOAD_PATH, source.getDataPath());
    }

    public void onComponentBegin(ExecutionComponent component) {
        componentStatus.put(component.getIri(), LP_EXEC.STATUS_RUNNING);
        writeDebugData(component);
    }

    private void writeDebugData(ExecutionComponent component) {
        if (pipeline.isDeleteWorkingData()) {
            return;
        }
        for (DataUnit dataUnit : component.getDataUnits()) {
            IRI dataUnitIri = valueFactory.createIRI(dataUnit.getIri());

            statements.add(dataUnitIri,
                    "http://etl.linkedpipes.com/ontology/dataPath",
                    dataUnit.getRelativeSaveDataPath());

            if (!dataUnit.getPort().isSaveDebugData()) {
                continue;
            }
            statements.add(dataUnitIri,
                    "http://etl.linkedpipes.com/ontology/debug",
                    dataUnit.getVirtualDebugPath());
        }
    }

    public void onComponentEnd(
            ExecutionComponent component, boolean cancelled) {
        if (cancelled) {
            componentStatus.put(component.getIri(), LP_EXEC.STATUS_CANCELLED);
        } else {
            componentStatus.put(component.getIri(), LP_EXEC.STATUS_FINISHED);
        }
    }

    public void onMapComponentSuccessful(ExecutionComponent component) {
        componentStatus.put(component.getIri(), LP_EXEC.STATUS_MAPPED);
        writeDebugData(component);
    }

    public void onComponentFailed(ExecutionComponent component) {
        componentStatus.put(component.getIri(), LP_EXEC.STATUS_FAILED);
    }

    public Statements getStatements() {
        Statements output = buildChangingValues();
        output.addAll(statements);
        return output;
    }

    private Statements buildChangingValues() {
        StatementsBuilder dynamic = Statements.arrayList().builder();
        dynamic.setDefaultGraph(iri);

        dynamic.addIri(iri,
                "http://etl.linkedpipes.com/ontology/status",
                executionStatus.getStatus().getIri());

        for (Map.Entry<String, String> entry : componentStatus.entrySet()) {
            IRI componentIri = valueFactory.createIRI(entry.getKey());
            dynamic.addIri(componentIri,
                    "http://etl.linkedpipes.com/ontology/status",
                    entry.getValue());
        }
        return dynamic;
    }

    public void save() throws IOException {
        Rdf4jUtils.save(getStatements(), file);
    }

}
