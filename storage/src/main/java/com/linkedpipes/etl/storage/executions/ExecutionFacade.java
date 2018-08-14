package com.linkedpipes.etl.storage.executions;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.unpacker.ExecutionSource;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ExecutionFacade implements ExecutionSource {

    private HttpExecutionSource source;

    @Autowired
    public ExecutionFacade(Configuration configuration) {
        this.source = new HttpExecutionSource(configuration);
    }

    @Override
    public Collection<Statement> getExecution(String iri) throws BaseException {
        return this.source.downloadExecution(iri);
    }

}
