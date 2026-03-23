package com.linkedpipes.plugin.transformer.rdfdifftoevent;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class RdfDiffToEvents implements Component, SequentialExecution {

    @Component.InputPort(iri = "LeftRdf")
    public SingleGraphDataUnit leftRdf;

    @Component.InputPort(iri = "RightRdf")
    public SingleGraphDataUnit rightRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Override
    public void execute() throws LpException {
        if (leftRdf.getRepository() == rightRdf.getRepository()
                && rightRdf.getRepository() == outputRdf.getRepository()) {
            executeSingleRepository();
        } else {
            executeDefault();
        }
    }

    private void executeSingleRepository() throws LpException {
        leftRdf.execute((connection) -> {
            rightRdf.execute((rightConnection) -> {
                copyData(connection, connection, leftRdf.getReadGraph());
                copyData(rightConnection, connection, rightRdf.getReadGraph());
            });
        });
    }

    private void copyData(
            RepositoryConnection input,
            RepositoryConnection output,
            Resource readGraph) {
        var statements = input.getStatements(
                null, null, null, readGraph);
        output.add(statements, outputRdf.getWriteGraph());
    }

    private void executeDefault() throws LpException {
        leftRdf.execute((leftConnection) -> {
            rightRdf.execute((rightConnection) -> {
                outputRdf.execute((outConnection) -> {
                    addNamespaces(leftConnection, outConnection);
                    addNamespaces(rightConnection, outConnection);
                    copyData(leftConnection, outConnection, leftRdf.getReadGraph());
                    copyData(rightConnection, outConnection, rightRdf.getReadGraph());
                });
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
