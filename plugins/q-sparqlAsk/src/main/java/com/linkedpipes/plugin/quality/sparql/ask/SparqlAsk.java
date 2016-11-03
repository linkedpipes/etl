package com.linkedpipes.plugin.quality.sparql.ask;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.SimpleDataset;

/**
 *
 */
public final class SparqlAsk implements Component.Sequential {

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.Configuration
    public SparqlAskConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SparqlAskVocabulary.HAS_SPARQL);
        }
        //
        final boolean ask;
        try {
            ask = inputRdf.execute((connection) -> {
                final BooleanQuery query = connection.prepareBooleanQuery(
                        QueryLanguage.SPARQL,
                        configuration.getQuery());
                final SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(inputRdf.getGraph());
                query.setDataset(dataset);
                return query.evaluate();
            });
        } catch (Throwable t) {
            throw exceptionFactory.failure("Can't evaluate SPARQL ask.", t);
        }
        if ((ask && configuration.isFailOnTrue())
                || (!ask && !configuration.isFailOnTrue())) {
            throw exceptionFactory.failure("Ask assertion failure.");
        }
    }

}
