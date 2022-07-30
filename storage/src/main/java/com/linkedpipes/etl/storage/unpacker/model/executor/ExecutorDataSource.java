package com.linkedpipes.etl.storage.unpacker.model.executor;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.library.rdf.StatementsBuilder;

/**
 * Represent a source of data for data unit.
 */
public class ExecutorDataSource {

    private String loadPath;

    private String execution;

    public ExecutorDataSource(String loadPath, String execution) {
        this.loadPath = loadPath;
        this.execution = execution;
    }

    public void write(String iri, StatementsBuilder builder) {
        builder.addType(iri, LP_EXEC.PORT_SOURCE);
        builder.add(iri, LP_EXEC.HAS_LOAD_PATH, loadPath);
        builder.addIri(iri, LP_EXEC.HAS_EXECUTION, execution);
    }

    public void setLoadPath(String loadPath) {
        this.loadPath = loadPath;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

}
