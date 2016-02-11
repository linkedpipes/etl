package com.linkedpipes.executor.rdf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 *
 * @author Škoda Petr
 */
public final class SesameUtils {

    public static List<Map<String, String>> executeSelect(RepositoryConnection connection, String query)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        final List<Map<String, String>> output = new LinkedList<>();
        // Evaluate query.
        final TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
        // Store result, convert everything into strings.
        while (result.hasNext()) {
            final BindingSet binding = result.next();
            final Map<String, String> bindingAsMap = new HashMap<>(binding.size());
            binding.getBindingNames().forEach((name) -> {
                bindingAsMap.put(name, binding.getValue(name).stringValue());
            });
            output.add(bindingAsMap);
        }
        return output;
    }

    public static void load(RepositoryConnection connection, File file, RDFFormat format)
            throws IOException, RDFParseException, RDFHandlerException {
        final RDFParser rdfParser = Rio.createParser(format);
        final RDFInserter inserter = new RDFInserter(connection);
        rdfParser.setRDFHandler(inserter);
        // Load file.
        try (InputStream input = new FileInputStream(file)) {
            rdfParser.parse(input, "http://localhost/");
        }
    }

    /**
     *
     * @param connection
     * @param file
     * @param format
     * @param graphUri Force insert of all statements into this graph.
     * @throws IOException
     * @throws RDFParseException
     * @throws RDFHandlerException
     */
    public static void load(RepositoryConnection connection, File file, RDFFormat format, IRI graphUri)
            throws IOException, RDFParseException, RDFHandlerException {
        final RDFParser rdfParser = Rio.createParser(format);
        final RDFInserter inserter = new RDFInserter(connection) {

            @Override
            protected void addStatement(Resource subj, IRI pred, Value obj, Resource ctxt) throws OpenRDFException {
                // Force context.
                super.addStatement(subj, pred, obj, graphUri);
            }

        };
        rdfParser.setRDFHandler(inserter);
        // Load file.
        try (InputStream input = new FileInputStream(file)) {
            rdfParser.parse(input, "http://localhost/");
        }
    }

    public static void store(RepositoryConnection connection, File file, RDFFormat format)
            throws IOException, RepositoryException, RDFHandlerException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            final RDFWriter writer = Rio.createWriter(format, outputStream);
            connection.export(writer);
        }
    }

}
