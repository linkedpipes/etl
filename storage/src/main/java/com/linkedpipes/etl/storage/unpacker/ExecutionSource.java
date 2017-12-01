package com.linkedpipes.etl.storage.unpacker;

import com.linkedpipes.etl.storage.BaseException;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

public interface ExecutionSource {

    Collection<Statement> getExecution(String executionIri)
            throws BaseException;

}
