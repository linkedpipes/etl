package com.linkedpipes.etl.executor.dataunit;

import com.linkedpipes.etl.executor.ExecutorException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;

public interface DataUnitInstanceSource {

    ManageableDataUnit getDataUnit(String iri) throws ExecutorException;

}
