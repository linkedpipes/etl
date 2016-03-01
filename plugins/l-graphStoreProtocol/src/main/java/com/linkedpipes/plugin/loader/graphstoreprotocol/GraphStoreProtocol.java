package com.linkedpipes.plugin.loader.graphstoreprotocol;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit.Entry;
import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import com.linkedpipes.etl.dpu.api.executable.SequentialExecution;
import com.linkedpipes.etl.dpu.api.extensions.AfterExecution;
import com.linkedpipes.etl.dpu.api.extensions.FaultTolerance;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.openrdf.model.Literal;
import org.openrdf.query.Binding;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class GraphStoreProtocol implements SequentialExecution {

    private final static String QUERY_SIZE_BINDING = "size";

    private final static String QUERY_SIZE = "SELECT (count(*) as ?"
            + QUERY_SIZE_BINDING + " ) \nWHERE { GRAPH <%s> { ?s ?p ?o } }";

    private final static String QUERY_ADD_GRAPH = "ADD <%s> TO <%s>";

    private final static String QUERY_CLEAR_GRAPH = "CLEAR GRAPH <%s>";

    private final static String UPLOAD_GRAPH_PREFIX
            = "http://temp.localhost/resource/graphStoreProtocol/uploadGraph/";

    private static final Logger LOG
            = LoggerFactory.getLogger(GraphStoreProtocol.class);

    @DataProcessingUnit.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @DataProcessingUnit.Configuration
    public GraphStoreProtocolConfiguration configuration;

    @DataProcessingUnit.Extension
    public FaultTolerance faultTolerance;

    public AfterExecution afterExecution;

    @Override
    public void execute(Context context) throws NonRecoverableException {
        if (inputFiles.size() != 1) {
            throw new ExecutionFailed("Invalid number of files on input.");
        }
        //
        final SPARQLRepository remoteRepository = new SPARQLRepository(
                configuration.getEndpointSelect(),
                configuration.getEndpointUpdate());
        if (configuration.isUseAuthentification()) {
            remoteRepository.setUsernameAndPassword(
                    configuration.getUserName(),
                    configuration.getPassword());
        }
        //
        faultTolerance.call(() -> {
            remoteRepository.initialize();
        });
        afterExecution.addAction(() -> {
            remoteRepository.shutDown();
        });
        //
        faultTolerance.call(() -> {
            final String graph = String.format(
                    QUERY_CLEAR_GRAPH,
                    configuration.getTargetGraphURI());
            executeRemoteUpdate(remoteRepository, graph);
        });
        for (Entry fileEntry : inputFiles) {
            LOG.info("Uploading: {}", fileEntry);
            uploadFile(remoteRepository, fileEntry,
                    configuration.getTargetGraphURI());
        }
    }

    private void uploadFile(SPARQLRepository remoteRepository,
            FilesDataUnit.Entry fileEntry, String graphUri)
            throws ExecutionFailed {
        // Log size of remote graph.
        final long beforeRemoteGraphSize = faultTolerance.call(() -> {
            return getRemoteGraphSize(remoteRepository,
                    configuration.getTargetGraphURI());
        });
        LOG.debug("Remote graph size: {}", beforeRemoteGraphSize);
        final String uploadGraph;
        if (useTempGraph()) {
            // Prepare name for upload graph - some random.
            uploadGraph = getTempGraph();
            LOG.info("Used temp graph: '{}'", uploadGraph);
            // Get size.
            final long beforeRemoteTempGraphSize = faultTolerance.call(() -> {
                return getRemoteGraphSize(remoteRepository, uploadGraph);
            });
            LOG.debug("Remote temp graph size: {}", beforeRemoteTempGraphSize);
        } else {
            uploadGraph = graphUri;
        }
        // Get file to upload.
        final File file = fileEntry.getPath();
        // Prepare target URL.
        final URL targetURL = prepareTargetURL(configuration.getEndpointCRUD());
        LOG.info("Target URL: '{}'", targetURL);
        // Upload file.
        faultTolerance.call(() -> {
            uploadFile(targetURL, file, uploadGraph);
        });
        if (useTempGraph()) {
            // Check upload file size.
            final long afterRemoteTempGraphSize = faultTolerance.call(() -> {
                return getRemoteGraphSize(remoteRepository, uploadGraph);
            });
            LOG.debug("Remote temp graph size: {}", afterRemoteTempGraphSize);
            // Copy remote files.
            LOG.info("Copying data from temp graph to target graph ...");
            faultTolerance.call(() -> {
                executeRemoteUpdate(remoteRepository,
                        String.format(QUERY_ADD_GRAPH, uploadGraph, graphUri));
            });
            // Clear temp graph.
            LOG.info("Clearing remote temp graph ...");
            faultTolerance.call(() -> {
                executeRemoteUpdate(remoteRepository,
                        String.format(QUERY_CLEAR_GRAPH, uploadGraph));
            });
        }
        // Get size of remote graph after add.
        final long afterRemoteGraphSize = faultTolerance.call(() -> {
            return getRemoteGraphSize(remoteRepository,
                    configuration.getTargetGraphURI());
        });
        LOG.debug("Remote graph size: {}", afterRemoteGraphSize);
    }

    private static void executeRemoteUpdate(SPARQLRepository remoteRepository,
            String query) throws ExecutionFailed, RepositoryException {
        RepositoryConnection connection = null;
        try {
            connection = remoteRepository.getConnection();
            LOG.debug("Executing update query on remote repository: {}", query);
            connection.prepareUpdate(QueryLanguage.SPARQL, query).execute();
        } catch (MalformedQueryException ex) {
            throw new ExecutionFailed("Can't execute size query.", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection.", ex);
                }
            }
        }
    }

    private long getRemoteGraphSize(SPARQLRepository remoteRepository,
            String graphURI) throws ExecutionFailed {
        // Connect to remote repository.
        RepositoryConnection connection = null;
        try {
            connection = remoteRepository.getConnection();
            final TupleQueryResult result = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    String.format(QUERY_SIZE, graphURI)).evaluate();
            if (!result.hasNext()) {
                // Empty result.
                throw new ExecutionFailed(
                        "Remote query for size does not return any value.");
            }
            final Binding binding = result.next().getBinding(QUERY_SIZE_BINDING);
            return ((Literal) binding.getValue()).longValue();
        } catch (MalformedQueryException ex) {
            throw new ExecutionFailed("Can't execute remote graph size query.",
                    ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection.", ex);
                }
            }
        }
    }

    private static URL prepareTargetURL(String base) throws ExecutionFailed {
        final String targetAsString = base;
        final URL targetURL;
        try {
            targetURL = new URL(targetAsString);
        } catch (MalformedURLException ex) {
            throw new ExecutionFailed("Malformed server uri.", ex);
        }
        return targetURL;
    }

    /**
     * Upload given file to given address.
     *
     * @param url
     * @param fileToUpload
     * @throws IOException
     * @throws ExecutionFailed
     */
    private void uploadFile(URL url, File fileToUpload, String targetGraph)
            throws IOException, ExecutionFailed {
        final HttpClient httpClient = new HttpClient();
        if (configuration.isUseAuthentification()) {
            httpClient.getState().setCredentials(
                    new AuthScope(url.getHost(), AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(
                            configuration.getUserName(),
                            configuration.getPassword()));
        }

        LOG.info("Uploading file to endpoint: {}", url.toString());

        PostMethod method = new PostMethod(url.toString());
        method.setParameter("Content-type", "application/xml");

        final List<Part> parts = new ArrayList<>();

        String fileFormat = "text/turtle";
        switch (configuration.getRepositoryType()) {
            case Virtuoso:
                // Virtuoso does not support chunked mode
                method.setContentChunked(false);
                // Target graph.
                parts.add(new StringPart("graph", targetGraph));
                break;
            case Fuseki:
                method.setContentChunked(true);
                // Target graph.
                parts.add(new StringPart("graph", targetGraph));
                break;
            case FusekiTrig: // Support for Fuseki 2.+
                method.setContentChunked(true);
                fileFormat = "text/trig";
                break;
        }
        // Add file.
        parts.add(new FilePart("res-file", fileToUpload, fileFormat, "UTF-8"));

        // Virtuoso - require "res-file"
        // final Part[] parts = {new FilePart("res-file", fileToUpload,
        //      "application/rdf+xml", "UTF-8") };
        // Fuseki
        // final Part[] parts = {new FilePart(fileToUpload.getName(),
        //      fileToUpload, "text/turtle", "UTF-8"),
        //      new StringPart("graph", targetGraph) };
        // FilePath.name = "rest-file" is required by Virtuso.
        //  Fuseky ignore this, so we can use it.
        // final Part[] parts = {new FilePart("res-file", fileToUpload,
        //      "text/turtle", "UTF-8"), new StringPart("graph", targetGraph) };
        final MultipartRequestEntity entity = new MultipartRequestEntity(
                parts.toArray(new Part[0]),
                method.getParams());
        method.setRequestEntity(entity);

        int responseCode = httpClient.executeMethod(method);
        final String response = method.getResponseBodyAsString();

        LOG.info("Response code: {}", responseCode);
        LOG.info("Response: {}", response);

        // Very simple response check.
        if (responseCode >= 500) {
            throw new ExecutionFailed("Request failed. Response code: {}"
                    + ". See logs for more details.", responseCode);
        }
    }

    private boolean useTempGraph() {
        return configuration.getRepositoryType()
                != GraphStoreProtocolConfiguration.RepositoryType.FusekiTrig;
    }

    private String getTempGraph() {
        return UPLOAD_GRAPH_PREFIX + Long.toString((new Date()).getTime());
    }

}
