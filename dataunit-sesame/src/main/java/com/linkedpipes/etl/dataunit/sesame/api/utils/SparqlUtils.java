package com.linkedpipes.etl.dataunit.sesame.api.utils;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.SimpleDataset;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public static List<Map<String, Value>> executeSelect(
            SingleGraphDataUnit dataUnit, String query) throws LpException {
        final List<Map<String, Value>> result = new LinkedList<>();
        dataUnit.execute((connection) -> {
            result.clear();
            // Prepare dataset.
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(dataUnit.getGraph());
            final TupleQuery tupleQuery = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, query);
            tupleQuery.setDataset(dataset);
            final TupleQueryResult tupleResult = tupleQuery.evaluate();
            while (tupleResult.hasNext()) {
                final Map<String, Value> row = new HashMap<>();
                for (Binding binding : tupleResult.next()) {
                    row.put(binding.getName(), binding.getValue());
                }
                result.add(row);
            }
        });
        return result;
    }

}
