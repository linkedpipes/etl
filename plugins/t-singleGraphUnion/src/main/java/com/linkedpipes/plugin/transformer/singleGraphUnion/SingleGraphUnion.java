package com.linkedpipes.plugin.transformer.singleGraphUnion;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryResult;

public class SingleGraphUnion implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Override
    public void execute() throws LpException {
        // TODO We can be more effective it the repositories are the same.
        inputRdf.execute((inConnection) -> {
            RepositoryResult<Statement> statements = inConnection.getStatements(
                    null, null, null, inputRdf.getReadGraph());
            outputRdf.execute((outConnection) -> {
                outConnection.add(statements, outputRdf.getWriteGraph());
            });
        });
    }

}
