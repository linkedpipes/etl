package com.linkedpipes.etl.storage.unpacker.model.execution;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.model.RdfValue;
import com.linkedpipes.etl.rdf.utils.pojo.Loadable;
import com.linkedpipes.etl.rdf.utils.pojo.LoaderException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExecutionComponent implements Loadable {

    private String iri;

    private List<ExecutionPort> ports = new ArrayList<>();

    @Override
    public void resource(String resource) throws LoaderException {
        iri = resource;
    }

    @Override
    public Loadable load(String predicate, RdfValue value)
            throws RdfUtilsException {
        switch (predicate) {
            case LP_EXEC.HAS_DATA_UNIT:
                ExecutionPort newPort = new ExecutionPort();
                ports.add(newPort);
                return newPort;
            default:
                return null;
        }
    }

    public String getIri() {
        return iri;
    }

    public List<ExecutionPort> getPorts() {
        return Collections.unmodifiableList(ports);
    }

    public ExecutionPort getPortByBinding(String binding) {
        for (ExecutionPort port : ports) {
            if (port.getBinding().equals(binding)) {
                return port;
            }
        }
        return null;
    }

}
