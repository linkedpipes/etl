package com.linkedpipes.plugin.transformer.singleGraphUnion;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class SingleGraphUnion implements Component, SequentialExecution {

    private final static String QUERY_COPY
            = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.OutputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Override
    public void execute() throws LpException {
        // TODO: We should use faster merge here!
        try (RepositoryConnection connection
                     = inputRdf.getRepository().getConnection()) {
            final Update update = connection.prepareUpdate(
                    QueryLanguage.SPARQL, QUERY_COPY);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(inputRdf.getReadGraph());
            dataset.setDefaultInsertGraph(outputRdf.getWriteGraph());
            update.setDataset(dataset);
            update.execute();
        }
    }

}
