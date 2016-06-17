package com.linkedpipes.plugin.loader.graphstoreprotocol;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit.Entry;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openrdf.model.Literal;
import org.openrdf.query.Binding;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.Component.ExecutionFailed;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;

/**
 *
 * @author Petr Škoda
 */
public class GraphStoreProtocol implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(GraphStoreProtocol.class);

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.Configuration
    public GraphStoreProtocolConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws ExecutionFailed {
        //
        if (inputFiles.size() > 1 && configuration.isReplace()) {
            throw exceptionFactory.failed("More then one file on input, "
                    + "with replace mode.");
        }
        //
        Long beforeSize = null;
        Long afterSize = null;
        if (configuration.isCheckSize()) {
            try {
                beforeSize = getGraphSize();
            } catch(IOException ex) {
                throw exceptionFactory.failed("Can't get graph size.", ex);
            }
        }
        for (final Entry entry : inputFiles) {
            final Optional<RDFFormat> optionalFormat
                    = Rio.getParserFormatForFileName(entry.getFileName());
            if (!optionalFormat.isPresent()) {
                throw exceptionFactory.failed(
                        "Can't determine format for file: {}", entry);
            }
            final String mimeType = optionalFormat.get().getDefaultMIMEType();
            //
            LOG.debug("Use repository: {}", configuration.getRepository());
            switch (configuration.getRepository()) {
                case BLAZEGRAPH:
                    uploadBlazegraph(configuration.getEndpoint(),
                            entry.toFile(),
                            mimeType,
                            configuration.getTargetGraph(),
                            configuration.isReplace());
                    break;
                case FUSEKI:
                    uploadFuseki(configuration.getEndpoint(),
                            entry.toFile(),
                            mimeType,
                            configuration.getTargetGraph(),
                            configuration.isReplace());
                    break;
                case VIRTUOSO:
                    uploadVirtuoso(configuration.getEndpoint(),
                            entry.toFile(),
                            mimeType,
                            configuration.getTargetGraph(),
                            configuration.isReplace());
                    break;
                default:
                    throw exceptionFactory.failed("Unknown repository type!");
            }
            if (configuration.isCheckSize()) {
                try {
                afterSize = getGraphSize();
            } catch(IOException ex) {
                throw exceptionFactory.failed("Can't get graph size.", ex);
            }
            }
        }
    }

    /**
     *
     * @return Size of a remote graph.
     * @throws com.linkedpipes.etl.dpu.api.DataProcessingUnit.ExecutionFailed
     * @throws IOException
     */
    private long getGraphSize() throws ExecutionFailed, IOException {
        final SPARQLRepository repository = new SPARQLRepository(
                configuration.getEndpointSelect());
        // Does nothing on SPARQLRepository.
        repository.initialize();
        //
        long size;
        try (final CloseableHttpClient httpclient = getHttpClient()) {
            repository.setHttpClient(httpclient);
            try (RepositoryConnection connection = repository.getConnection()) {
                final String query = "SELECT (count(*) as ?count) WHERE { GRAPH "
                        + "<" + configuration.getTargetGraph() + "> { ?s ?p ?o } }";
                final TupleQueryResult result = connection.prepareTupleQuery(
                        QueryLanguage.SPARQL, query).evaluate();
                if (!result.hasNext()) {
                    // Empty result.
                    throw exceptionFactory.failed(
                            "Remote query for size does not return any value.");
                }
                final Binding binding = result.next().getBinding("count");
                size = ((Literal) binding.getValue()).longValue();
            }
        } finally {
            repository.shutDown();
        }
        LOG.info("Graph size: {}", size);
        return size;
    }

    private void uploadBlazegraph(String url, File file, String mimeType,
            String graph, boolean update) throws ExecutionFailed {
        try {
            url += "?context-uri=" + URLEncoder.encode(graph, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failed("URLEncoder failed.", ex);
        }
        //
        final HttpEntityEnclosingRequestBase httpMethod;
        if (update) {
            // Blaze graph delte statements based on provided qyeru.
            final String query = "CONSTRUCT{ ?s ?p ?o} FROM <" + graph
                    + "> WHERE { ?s ?p ?o }";
            try {
                url += "&query=" + URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw exceptionFactory.failed("URLEncoder failed.", ex);
            }
            //
            httpMethod = new HttpPut(url);
        } else {
            httpMethod = new HttpPost(url);
        }
        LOG.debug("url: {}", url);
        httpMethod.setEntity(new FileEntity(file, ContentType.create(mimeType)));
        //
        executeHttp(httpMethod);
    }

    private void uploadFuseki(String url, File file, String mimeType,
            String graph, boolean update) throws ExecutionFailed {
        //
        try {
            url += "?graph=" + URLEncoder.encode(graph, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failed("URLEncoder failed.", ex);
        }
        //
        final HttpEntityEnclosingRequestBase httpMethod;
        if (update) {
            httpMethod = new HttpPut(url);
        } else {
            httpMethod = new HttpPost(url);
        }
        final FileBody fileBody = new FileBody(file,
                ContentType.create(mimeType), "file");
        final HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .build();
        httpMethod.setEntity(entity);
        //
        executeHttp(httpMethod);
    }

    public void uploadVirtuoso(String url, File file, String mimeType,
            String graph, boolean update) throws ExecutionFailed {
        //
        try {
            url += "?graph=" + URLEncoder.encode(graph, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failed("URLEncoder failed.", ex);
        }
        //
        final HttpEntityEnclosingRequestBase httpMethod;
        if (update) {
            httpMethod = new HttpPut(url);
        } else {
            httpMethod = new HttpPost(url);
        }
        //
        httpMethod.setEntity(new FileEntity(file, ContentType.create(mimeType)));
        //
        executeHttp(httpMethod);
    }

    private void executeHttp(HttpEntityEnclosingRequestBase httpMethod) throws ExecutionFailed {
        try (final CloseableHttpClient httpclient = getHttpClient()) {
            try (final CloseableHttpResponse response = httpclient.execute(httpMethod)) {
                try {
                    LOG.debug("Response:\n {} ", EntityUtils.toString(response.getEntity()));
                } catch (java.net.SocketException ex) {
                    LOG.error("Can't read response.", ex);
                }
                LOG.info("Response code: {} phrase: {}",
                        response.getStatusLine().getStatusCode(),
                        response.getStatusLine().getReasonPhrase());
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < 200 && statusCode >= 300) {
                    throw exceptionFactory.failed("Can't upload data, reason: {}",
                            response.getStatusLine().getReasonPhrase());
                }
            }
        } catch (IOException | ParseException ex) {
            throw exceptionFactory.failed("Can't execute request.", ex);
        }
    }

    /**
     *
     * @return Must be closed after use.
     */
    private CloseableHttpClient getHttpClient() {
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        if (configuration.isUseAuthentification()) {
            credsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(
                            configuration.getUserName(),
                            configuration.getPassword()));
        }
        return HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
    }

}
