package com.linkedpipes.plugin.transformer.singleGraphUnion;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.executable.SimpleExecution;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author Petr Škoda
 */
public class SingleGraphUnion implements SimpleExecution {

    private final static String QUERY_COPY
            = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.InputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Override
    public void execute(Context context) throws NonRecoverableException {
        // TODO: We should use faster merge here!
        try (RepositoryConnection connection
                = inputRdf.getRepository().getConnection()) {
            final Update update = connection.prepareUpdate(
                    QueryLanguage.SPARQL, QUERY_COPY);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(inputRdf.getGraph());
            dataset.setDefaultInsertGraph(outputRdf.getGraph());
            update.setDataset(dataset);
            update.execute();
        }
    }

}
