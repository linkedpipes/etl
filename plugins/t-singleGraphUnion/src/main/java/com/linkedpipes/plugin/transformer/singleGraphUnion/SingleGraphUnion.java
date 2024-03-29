package com.linkedpipes.plugin.transformer.singleGraphUnion;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class SingleGraphUnion implements Component, SequentialExecution {

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Override
    public void execute() throws LpException {
        if (inputRdf.getRepository() == outputRdf.getRepository()) {
            executeSingleRepository();
        } else {
            executeDefault();
        }
    }

    private void executeSingleRepository() throws LpException {
        inputRdf.execute((connection) -> {
            copyData(connection, connection);
        });
    }

    private void copyData(
            RepositoryConnection input,
            RepositoryConnection output) {
        var statements = input.getStatements(
                null, null, null, inputRdf.getReadGraph());
        output.add(statements, outputRdf.getWriteGraph());
    }

    private void executeDefault() throws LpException {
        inputRdf.execute((inConnection) -> {
            outputRdf.execute((outConnection) -> {
                addNamespaces(inConnection, outConnection);
                copyData(inConnection, outConnection);
            });
        });
    }

    private void addNamespaces(
            RepositoryConnection input,
            RepositoryConnection output) {
        var namespaces = input.getNamespaces();
        while (namespaces.hasNext()) {
            Namespace namespace = namespaces.next();
            output.setNamespace(
                    namespace.getPrefix(),
                    namespace.getName());
        }
    }

}
