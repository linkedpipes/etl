package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.execution.model.DataUnit;
import com.linkedpipes.etl.executor.execution.model.ExecutionComponent;
import com.linkedpipes.etl.executor.execution.model.ExecutionModel;
import com.linkedpipes.etl.executor.pipeline.model.*;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jUtils;
import com.linkedpipes.etl.rdf4j.Statements;
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
    private final Statements statements = Statements.ArrayList();

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
        this.bindToExecution();
    }

    private void bindToExecution() {
        this.iri = this.valueFactory.createIRI(execution.getIri());
        this.statements.setDefaultGraph(this.iri);
        this.statements.addIri(this.iri, RDF.TYPE, ETL_PREFIX + "Execution");
    }

    public void onPipelineLoaded(PipelineModel pipeline) {
        this.pipeline = pipeline;
        this.statements.add(
                this.iri,
                LP_EXEC.HAS_DELETE_WORKING_DATA,
                this.valueFactory.createLiteral(pipeline.isDeleteWorkingData()));
        this.statements.addIri(
                this.iri, ETL_PREFIX + "pipeline", pipeline.getIri());
        for (PipelineComponent component : pipeline.getComponents()) {
            this.addPipelineComponent(component);
        }
    }

    private void addPipelineComponent(PipelineComponent component) {
        if (component.getExecutionType() == ExecutionType.SKIP) {
            return;
        }
        IRI componentIri = this.valueFactory.createIRI(component.getIri());

        this.statements.add(this.iri, LP_PREFIX + "component", componentIri);
        this.statements.addIri(componentIri, RDF.TYPE, LP_PIPELINE.COMPONENT);

        if (component.getExecution() != null) {
            this.statements.addIri(
                    componentIri,
                    LP_EXEC.HAS_EXECUTION,
                    component.getExecution());
        }

        int order = component.getExecutionOrder();
        this.statements.add(
                componentIri,
                LP_PREFIX + "order",
                this.valueFactory.createLiteral(order));

        for (Port port : component.getPorts()) {
            this.addPipelinePort(componentIri, port);
        }

        this.componentStatus.put(component.getIri(), LP_EXEC.STATUS_QUEUED);
    }

    private void addPipelinePort(IRI componentIri, Port port) {
        IRI portIri = this.valueFactory.createIRI(port.getIri());

        this.statements.add(componentIri, ETL_PREFIX + "dataUnit", portIri);
        this.statements.addIri(portIri, RDF.TYPE, ETL_PREFIX + "DataUnit");
        this.statements.addString(
                portIri, ETL_PREFIX + "binding", port.getBinding());

        DataSource source = port.getDataSource();
        if (source != null) {
            addPipelinePortSource(portIri, source);
        }
    }

    private void addPipelinePortSource(IRI portIri, DataSource source) {
        this.statements.addIri(
                portIri, LP_EXEC.HAS_EXECUTION, source.getExecution());
        this.statements.addString(
                portIri, LP_EXEC.HAS_LOAD_PATH, source.getDataPath());
    }

    public void onComponentBegin(ExecutionComponent component) {
        this.componentStatus.put(component.getIri(), LP_EXEC.STATUS_RUNNING);
        this.writeDebugData(component);
    }

    private void writeDebugData(ExecutionComponent component) {
        if (this.pipeline.isDeleteWorkingData()) {
            return;
        }
        for (DataUnit dataUnit : component.getDataUnits()) {
            IRI dataUnitIri = this.valueFactory.createIRI(dataUnit.getIri());

            this.statements.addString(dataUnitIri,
                    "http://etl.linkedpipes.com/ontology/dataPath",
                    dataUnit.getRelativeSaveDataPath());

            if (!dataUnit.getPort().isSaveDebugData()) {
                continue;
            }
            this.statements.addString(dataUnitIri,
                    "http://etl.linkedpipes.com/ontology/debug",
                    dataUnit.getVirtualDebugPath());
        }
    }

    public void onComponentEnd(ExecutionComponent component) {
        this.componentStatus.put(component.getIri(), LP_EXEC.STATUS_FINISHED);
    }

    public void onMapComponentSuccessful(ExecutionComponent component) {
        this.componentStatus.put(component.getIri(), LP_EXEC.STATUS_MAPPED);
        this.writeDebugData(component);
    }

    public void onComponentFailed(ExecutionComponent component) {
        this.componentStatus.put(component.getIri(), LP_EXEC.STATUS_FAILED);
    }

    public Statements getStatements() {
        Statements output = buildChangingValues();
        output.addAll(this.statements);
        return output;
    }

    private Statements buildChangingValues() {
        Statements dynamic = Statements.ArrayList();
        dynamic.setDefaultGraph(this.iri);

        dynamic.addIri(this.iri,
                "http://etl.linkedpipes.com/ontology/status",
                this.executionStatus.getStatus().getIri());

        for (Map.Entry<String, String> entry : this.componentStatus.entrySet()) {
            IRI componentIri = valueFactory.createIRI(entry.getKey());
            dynamic.addIri(componentIri,
                    "http://etl.linkedpipes.com/ontology/status",
                    entry.getValue());
        }
        return dynamic;
    }

    public void save() throws IOException {
        Rdf4jUtils.save(this.getStatements(), this.file);
    }

}
