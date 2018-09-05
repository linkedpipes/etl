package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class PipelineLoader {

    private static final Logger LOG
            = LoggerFactory.getLogger(PipelineLoader.class);

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
        File file = this.getPipelineFile();
        Statements pipeline = this.loadPipelineFile(file);
        this.searchForPipelineAndMetadata(pipeline);

        this.execution.setPipeline(this.pipelineResource);
        if (this.pipelineResource == null) {
            LOG.error("Missing pipeline resource for: {}",
                    this.execution.getId());
            return;
        }

        Statements output = Statements.ArrayList();
        output.setDefaultGraph(execution.getListGraph());
        output.addAll(this.processMetadata(pipeline));
        output.addAll(this.processPipeline());
        this.selectUsedLabels(pipeline).forEach(output::integrate);

        this.execution.setPipelineStatements(output);
    }

    private File getPipelineFile() throws MonitorException {
        File pipelineFile = new File(
                this.execution.getDirectory(), "pipeline.trig");
        if (pipelineFile.exists()) {
            return pipelineFile;
        }
        File definitionFile = new File(
                this.execution.getDirectory(), "definition/definition.trig");
        if (definitionFile.exists()) {
            return definitionFile;
        }
        throw new MonitorException(
                "Missing pipeline file for execution: {}",
                this.execution.getIri());
    }

    private Statements loadPipelineFile(File file) throws MonitorException {
        Statements statements = Statements.ArrayList();
        try {
            statements.addAll(file);
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
                            this.pipelineResource = st.getSubject();
                            this.subjectWithLabels.add(st.getSubject());
                            break;
                        case LP_PIPELINE.EXECUTION_METADATA:
                            this.pipelineMetadata = st.getSubject();
                            break;
                    }
                });
    }

    private Statements processMetadata(Statements pipeline) {
        if (this.pipelineMetadata == null) {
            return Statements.EmptyReadOnly();
        }
        Statements statements = Statements.ArrayList();
        statements.setDefaultGraph(this.execution.getListGraph());
        statements.addIri(
                this.pipelineMetadata,
                RDF.TYPE,
                LP_PIPELINE.EXECUTION_METADATA);
        statements.add(
                this.pipelineResource,
                LP_PIPELINE.HAS_EXECUTION_METADATA,
                this.pipelineMetadata);

        pipeline.stream()
                .filter(st -> st.getSubject().equals(this.pipelineMetadata))
                .forEach(st -> {
                    switch (st.getPredicate().stringValue()) {
                        case LP_EXEC.HAS_TARGET_COMPONENT:
                            statements.add(
                                    this.pipelineMetadata,
                                    LP_EXEC.HAS_TARGET_COMPONENT,
                                    st.getObject());
                            this.subjectWithLabels.add(
                                    (Resource)st.getObject());
                            break;
                        default:
                            statements.integrate(st);
                            break;
                    }
                });
        return statements;
    }

    private Statements processPipeline() {
        Statements statements = Statements.ArrayList();
        statements.setDefaultGraph(this.execution.getListGraph());
        statements.addIri(
                this.pipelineResource,
                RDF.TYPE,
                LP_PIPELINE.PIPELINE);
        return statements;
    }

    private Stream<Statement> selectUsedLabels(Statements pipeline) {
        return pipeline.stream()
                .filter(st -> st.getPredicate().equals(SKOS.PREF_LABEL))
                .filter(st -> this.subjectWithLabels.contains(st.getSubject()));
    }

}
