package com.linkedpipes.etl.unpacker;

import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

public interface ExecutionSource {

    Collection<Statement> getExecution(String executionIri)
            throws StorageException;

}
