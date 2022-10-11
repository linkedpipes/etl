package com.linkedpipes.etl.unpacker.model.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.unpacker.rdf.Loadable;
import org.eclipse.rdf4j.model.Value;

/**
 * TODO Rename to DataUnit to align with the vocabulary.
 */
public class ExecutionPort implements Loadable {

    private String binding;

    private String execution;

    private String dataPath;

    private String loadPath;

    @Override
    public Loadable load(String predicate, Value value) {
        switch (predicate) {
            // TODO Align with case LP_PIPELINE.HAS_BINDING
            case "http://etl.linkedpipes.com/ontology/binding":
                binding = value.stringValue();
                return null;
            case LP_EXEC.HAS_EXECUTION:
                execution = value.stringValue();
                return null;
            case LP_EXEC.HAS_DATA_PATH:
                dataPath = value.stringValue();
                return null;
            case LP_EXEC.HAS_LOAD_PATH:
                loadPath = value.stringValue();
                return null;
            default:
                return null;
        }
    }

    public String getBinding() {
        return binding;
    }

    public String getExecution() {
        return execution;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getLoadPath() {
        return loadPath;
    }

}
