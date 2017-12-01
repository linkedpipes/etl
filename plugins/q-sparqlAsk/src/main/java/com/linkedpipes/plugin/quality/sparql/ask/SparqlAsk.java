package com.linkedpipes.plugin.quality.sparql.ask;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;

public final class SparqlAsk implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputRdf")
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
                dataset.addDefaultGraph(inputRdf.getReadGraph());
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
