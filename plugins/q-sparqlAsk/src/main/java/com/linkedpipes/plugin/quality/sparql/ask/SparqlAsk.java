package com.linkedpipes.plugin.quality.sparql.ask;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.SimpleDataset;

/**
 *
 * @author Petr Škoda
 */
public final class SparqlAsk implements Component.Sequential {

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.Configuration
    public SparqlAskConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws NonRecoverableException {
        final boolean ask = inputRdf.execute((connection) -> {
            final BooleanQuery query = connection.prepareBooleanQuery(
                    QueryLanguage.SPARQL,
                    configuration.getQuery());

            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(inputRdf.getGraph());
            query.setDataset(dataset);

            return query.evaluate();
        });
        if ((ask && configuration.isFailOnTrue())
                || (!ask && !configuration.isFailOnTrue())) {
            throw exceptionFactory.failed("Ask assertion failed.");
        }
    }

}
