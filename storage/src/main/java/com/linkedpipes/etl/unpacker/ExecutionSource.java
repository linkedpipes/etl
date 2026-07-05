package com.linkedpipes.etl.unpacker;

import com.linkedpipes.etl.storage.StorageException;
import java.util.Collection;
import org.eclipse.rdf4j.model.Statement;

public interface ExecutionSource {

    Collection<Statement> getExecution(String executionIri) throws StorageException;
}
