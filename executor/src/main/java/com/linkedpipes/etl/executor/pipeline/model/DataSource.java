package com.linkedpipes.etl.executor.pipeline.model;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.BackendRdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;

/**
 * Represent a source of data for given DataUnit. Used
 * for mapped DataUnits.
 */
public class DataSource implements Loadable {

    private String dataPath;

    private String execution;

    public DataSource() {
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getExecution() {
        return execution;
    }

    @Override
    public Loadable load(String predicate, BackendRdfValue object)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_EXEC.HAS_LOAD_PATH:
                this.dataPath = object.asString();
                return null;
            case LP_EXEC.HAS_EXECUTION:
                this.execution = object.asString();
                return null;
            default:
                return null;
        }
    }

}
