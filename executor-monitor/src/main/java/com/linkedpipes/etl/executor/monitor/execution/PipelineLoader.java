package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class PipelineLoader {

    private Resource pipelineResource = null;

    private Resource pipelineMetadata = null;

    private final Execution execution;

    /**
     * Stores resources of which labels should we put to output.
     */
    private List<Resource> subjectWithLabels = new ArrayList<>(2);

    public PipelineLoader(Execution execution) {
        this.execution = execution;
    }

    public void loadPipelineIntoExecution() throws MonitorException {
        File file = getPipelineFile();
        Statements pipeline = loadPipelineFile(file);
        searchForPipelineAndMetadata(pipeline);

        execution.setPipeline(pipelineResource);
        if (pipelineResource == null) {
            throw new MonitorException(
                    "Missing pipeline resource for: {}",
                    execution.getId());
        }

        StatementsBuilder output = Statements.arrayList().builder();
        output.setDefaultGraph(execution.getListGraph());
        output.addAll(processMetadata(pipeline));
        output.addAll(processPipeline());
        selectUsedLabels(pipeline).forEach(output::addToDefaultGraph);

        execution.setPipelineStatements(output);
    }

    private File getPipelineFile() throws MonitorException {
        File pipelineFile = new File(execution.getDirectory(), "pipeline.trig");
        if (pipelineFile.exists()) {
            return pipelineFile;
        }
        File definitionFile = new File(
                execution.getDirectory(), "definition/definition.trig");
        if (definitionFile.exists()) {
            return definitionFile;
        }
        File definitionFileJsonld = new File(
                execution.getDirectory(), "definition/definition.jsonld");
        if (definitionFileJsonld.exists()) {
            return definitionFileJsonld;
        }
        throw new MonitorException(
                "Missing pipeline file for execution: {}",
                execution.getIri());
    }

    private Statements loadPipelineFile(File file) throws MonitorException {
        Statements statements = Statements.arrayList();
        try {
            statements.file().addAll(file);
        } catch (IOException ex) {
            throw new MonitorException("Can't load pipeline.", ex);
        }
        return statements;
    }

    private void searchForPipelineAndMetadata(Statements pipeline) {
        pipeline.stream()
                .filter(st -> st.getPredicate().equals(RDF.TYPE))
                .forEach(st -> {
                    String object = st.getObject().stringValue();
                    switch (object) {
                        case LP_PIPELINE.PIPELINE:
                            pipelineResource = st.getSubject();
                            subjectWithLabels.add(st.getSubject());
                            break;
                        case LP_PIPELINE.EXECUTION_METADATA:
                            pipelineMetadata = st.getSubject();
                            break;
                        default:
                            break;
                    }
                });
    }

    private Statements processMetadata(Statements pipeline) {
        if (pipelineMetadata == null) {
            return Statements.readOnly(Collections.emptyList());
        }
        StatementsBuilder statements = Statements.arrayList().builder();
        statements.setDefaultGraph(execution.getListGraph());
        statements.addIri(
                pipelineMetadata,
                RDF.TYPE,
                LP_PIPELINE.EXECUTION_METADATA);
        statements.add(
                pipelineResource,
                LP_PIPELINE.HAS_EXECUTION_METADATA,
                pipelineMetadata);

        pipeline.stream()
                .filter(st -> st.getSubject().equals(pipelineMetadata))
                .forEach(st -> {
                    switch (st.getPredicate().stringValue()) {
                        case LP_EXEC.HAS_TARGET_COMPONENT:
                            statements.add(
                                    pipelineMetadata,
                                    LP_EXEC.HAS_TARGET_COMPONENT,
                                    st.getObject());
                            subjectWithLabels.add(
                                    (Resource) st.getObject());
                            break;
                        default:
                            statements.add(st);
                            break;
                    }
                });
        return statements;
    }

    private Statements processPipeline() {
        StatementsBuilder statements = Statements.arrayList().builder();
        statements.setDefaultGraph(execution.getListGraph());
        statements.addIri(
                pipelineResource,
                RDF.TYPE,
                LP_PIPELINE.PIPELINE);
        return statements;
    }

    private Stream<Statement> selectUsedLabels(Statements pipeline) {
        return pipeline.stream()
                .filter(st -> st.getPredicate().equals(SKOS.PREF_LABEL))
                .filter(st -> subjectWithLabels.contains(st.getSubject()));
    }

}
