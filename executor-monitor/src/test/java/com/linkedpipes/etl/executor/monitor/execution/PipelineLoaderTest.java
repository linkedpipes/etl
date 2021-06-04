package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.TestUtils;
import com.linkedpipes.etl.rdf4j.Statements;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PipelineLoaderTest {

    @Test
    public void loadPipeline() throws MonitorException {
        Execution execution = new Execution();
        execution.setIri("http://execution");
        execution.setDirectory(TestUtils.resource("execution"));
        PipelineLoader loader = new PipelineLoader(execution);
        loader.loadPipelineIntoExecution();
        //
        Statements expected = Statements.arrayList();
        expected.setDefaultGraph(execution.getListGraph());
        expected.addIri("http://pipeline", RDF.TYPE, LP_PIPELINE.PIPELINE);
        expected.addString("http://pipeline", SKOS.PREF_LABEL, "Pipeline");
        String meta = "http://pipeline/metadata";
        expected.addIri(meta, RDF.TYPE, LP_PIPELINE.EXECUTION_METADATA);
        expected.addIri(meta, LP_EXEC.HAS_TARGET_COMPONENT, "http://component");
        expected.addString("http://component", SKOS.PREF_LABEL, "Component");
        //
        Statements actual = new Statements(execution.getPipelineStatements());
        Assertions.assertTrue(actual.containsAllLogMissing(expected));
    }

}
