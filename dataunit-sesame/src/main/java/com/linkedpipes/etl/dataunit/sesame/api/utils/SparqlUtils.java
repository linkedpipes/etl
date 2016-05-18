package com.linkedpipes.etl.dataunit.sesame.api.utils;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Value;
import org.openrdf.query.AbstractTupleQueryResultHandler;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.util.Repositories;

/**
 *
 * @author Petr Škoda
 */
public final class SparqlUtils {

    private SparqlUtils() {
    }

    /**
     * Execute given SPARQL select query and return the result.
     *
     * @param dataUnit
     * @param query
     * @return
     * @throws NonRecoverableException
     */
    public static List<Map<String, Value>> executeSelect(
            SingleGraphDataUnit dataUnit, String query)
            throws NonRecoverableException {
        final List<Map<String, Value>> result = new LinkedList<>();
        dataUnit.execute(() -> {
            result.clear();
            Repositories.tupleQuery(dataUnit.getRepository(),
                    query, new AbstractTupleQueryResultHandler() {
                @Override
                public void handleSolution(BindingSet bindingSet)
                        throws TupleQueryResultHandlerException {
                    final Map<String, Value> row = new HashMap<>();
                    for (Binding binding : bindingSet) {
                        row.put(binding.getName(), binding.getValue());
                    }
                }
            });
        });
        return result;
    }

}
