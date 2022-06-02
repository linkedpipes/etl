package com.linkedpipes.plugin.loader.graphstoreprotocol;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

class GraphSize {

    public static long getGraphSize(
            CloseableHttpClient httpClient, String endpoint, String graph)
            throws LpException {
        SPARQLRepository repository = new SPARQLRepository(endpoint);
        repository.setHttpClient(httpClient);
        repository.init();
        //
        long size;
        try (RepositoryConnection connection = repository.getConnection()) {
            String query = getQuery(graph);
            TupleQueryResult result = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, query).evaluate();
            if (!result.hasNext()) {
                throw new LpException(
                        "Remote query for size does not return any value.");
            }
            Binding binding = result.next().getBinding("count");
            size = ((Literal) binding.getValue()).longValue();
        } finally {
            repository.shutDown();
        }
        return size;
    }

    protected static String getQuery(String graph) {
        if (graph == null) {
            return "SELECT (count(*) as ?count) WHERE { ?s ?p ?o }";
        } else {
            return "SELECT (count(*) as ?count) WHERE { "
                    + "GRAPH <" + graph + "> { ?s ?p ?o } }";
        }
    }

}
