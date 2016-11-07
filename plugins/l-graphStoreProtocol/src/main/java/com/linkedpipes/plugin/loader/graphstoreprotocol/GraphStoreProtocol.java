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
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;

/**
 */
public class GraphStoreProtocol implements Component.Sequential {

    private static final Logger LOG
            = LoggerFactory.getLogger(GraphStoreProtocol.class);

    @Component.ContainsConfiguration
    @Component.InputPort(id = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.Configuration
    public GraphStoreProtocolConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getEndpoint() == null
                || configuration.getEndpoint().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    GraphStoreProtocolVocabulary.HAS_CRUD);
        }
        //
        if (inputFiles.size() > 1 && configuration.isReplace()) {
            throw exceptionFactory.failure("Only one file can be uploaded"
                    + "with replace mode.");
        }
        //
        Long beforeSize = null;
        Long afterSize = null;
        if (configuration.isCheckSize()) {
            try {
                beforeSize = getGraphSize();
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't get graph size.", ex);
            }
        }
        for (final Entry entry : inputFiles) {
            final Optional<RDFFormat> rdfFormat
                    = Rio.getParserFormatForFileName(entry.getFileName());
            if (!rdfFormat.isPresent()) {
                throw exceptionFactory.failure(
                        "Can't determine format for file: {}", entry);
            }
            if (rdfFormat.get().supportsContexts()) {
                throw exceptionFactory.failure(
                        "Quad-based formats are not supported.");
            }
            final String mimeType = rdfFormat.get().getDefaultMIMEType();
            //
            LOG.debug("Use repository: {}", configuration.getRepository());
            switch (configuration.getRepository()) {
                case BLAZEGRAPH:
                    uploadBlazegraph(configuration.getEndpoint(),
                            entry.toFile(), mimeType,
                            configuration.getTargetGraph(),
                            configuration.isReplace());
                    break;
                case FUSEKI:
                    uploadFuseki(configuration.getEndpoint(),
                            entry.toFile(), mimeType,
                            configuration.getTargetGraph(),
                            configuration.isReplace());
                    break;
                case VIRTUOSO:
                    uploadVirtuoso(configuration.getEndpoint(),
                            entry.toFile(), mimeType,
                            configuration.getTargetGraph(),
                            configuration.isReplace());
                    break;
                default:
                    throw exceptionFactory.failure("Unknown repository type!");
            }
            if (configuration.isCheckSize()) {
                try {
                    afterSize = getGraphSize();
                } catch (IOException ex) {
                    throw exceptionFactory.failure("Can't get graph size.", ex);
                }
            }
        }
        if (beforeSize != null) {
            LOG.info("Before graph size: {}", beforeSize);
        }
        if (afterSize != null) {
            LOG.info("After graph size: {}", afterSize);
        }
    }

    /**
     * @return Size of a remote graph.
     * @throws LpException
     * @throws IOException
     */
    private long getGraphSize() throws LpException, IOException {
        final SPARQLRepository repository = new SPARQLRepository(
                configuration.getEndpointSelect());
        // Does nothing on SPARQLRepository.
        repository.initialize();
        //
        long size;
        try (final CloseableHttpClient httpclient = getNonAuthHttpClient()) {
            repository.setHttpClient(httpclient);
            try (RepositoryConnection connection = repository.getConnection()) {
                final String query = "SELECT (count(*) as ?count) WHERE { "
                        + "GRAPH <" + configuration.getTargetGraph()
                        + "> { ?s ?p ?o } }";
                final TupleQueryResult result = connection.prepareTupleQuery(
                        QueryLanguage.SPARQL, query).evaluate();
                if (!result.hasNext()) {
                    // Empty result.
                    throw exceptionFactory.failure(
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
            String graph, boolean replace) throws LpException {
        LOG.info("Blazegraph: {} {} {} {} {}", url, file.getName(), mimeType,
                graph, replace);
        //
        try {
            url += "?context-uri=" + URLEncoder.encode(graph, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failure("URLEncoder failure.", ex);
        }
        //
        final HttpEntityEnclosingRequestBase httpMethod;
        if (replace) {
            // Blaze graph delete statements based on provided query.
            final String query = "CONSTRUCT{ ?s ?p ?o} FROM <" + graph
                    + "> WHERE { ?s ?p ?o }";
            try {
                url += "&query=" + URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw exceptionFactory.failure("URLEncoder failure.", ex);
            }
            //
            httpMethod = new HttpPut(url);
        } else {
            httpMethod = new HttpPost(url);
        }
        LOG.debug("url: {}", url);
        httpMethod.setEntity(new FileEntity(file,
                ContentType.create(mimeType)));
        //
        executeHttp(httpMethod);
    }

    private void uploadFuseki(String url, File file, String mimeType,
            String graph, boolean replace) throws LpException {
        LOG.info("Fuseki: {} {} {} {} {}", url, file.getName(), mimeType,
                graph, replace);
        //
        try {
            url += "?graph=" + URLEncoder.encode(graph, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failure("URLEncoder failure.", ex);
        }
        //
        final HttpEntityEnclosingRequestBase httpMethod;
        if (replace) {
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

    private void uploadVirtuoso(String url, File file, String mimeType,
            String graph, boolean replace) throws LpException {
        LOG.info("Virtuoso: {} {} {} {} {}", url, file.getName(), mimeType,
                graph, replace);
        //
        try {
            url += "?graph=" + URLEncoder.encode(graph, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failure("URLEncoder failure.", ex);
        }
        //
        final HttpEntityEnclosingRequestBase httpMethod;
        if (replace) {
            httpMethod = new HttpPut(url);
        } else {
            httpMethod = new HttpPost(url);
        }
        //
        httpMethod.setEntity(new FileEntity(file,
                ContentType.create(mimeType)));
        //
        executeHttp(httpMethod);
    }

    private void executeHttp(HttpEntityEnclosingRequestBase httpMethod)
            throws LpException {
        final CloseableHttpClient httpClient;
        // We use shared context.
        final HttpClientContext context = HttpClientContext.create();
        if (!configuration.isUseAuthentification()) {
            httpClient = HttpClients.custom().build();
        } else {
            // Use preemptive authentication.
            final CredentialsProvider creds = new BasicCredentialsProvider();
            creds.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(
                            configuration.getUserName(),
                            configuration.getPassword()));
            //
            final RequestConfig requestConfig = RequestConfig.custom()
                    .setAuthenticationEnabled(true).build();
            //
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultCredentialsProvider(creds)
                    .build();
        }
        // Do an empty request just to get the validation into a cache.
        // This is requires as for example Virtuoso will refuse the first
        // request and ask for authorization. However as the first request
        // can be too big - it would looks like a failure to us
        // (as Virtuoso just close the connection before reading all the data).
        if (configuration.isUseAuthentification()) {
            final HttpEntityEnclosingRequestBase emptyRequest
                    = new HttpPut(configuration.getEndpoint());
            try (final CloseableHttpResponse response
                    = httpClient.execute(emptyRequest, context)) {
            } catch (Exception ex) {
                LOG.info("Exception during first empty request:", ex);
            }
        }
        //
        try (final CloseableHttpResponse response
                = httpClient.execute(httpMethod, context)) {
            try {
                LOG.debug("Response:\n {} ",
                        EntityUtils.toString(response.getEntity()));
            } catch (java.net.SocketException ex) {
                LOG.error("Can't read response.", ex);
            }
            LOG.info("Response code: {} phrase: {}",
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase());
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < 200 && statusCode >= 300) {
                throw exceptionFactory.failure(
                        "Can't upload data, reason: {}",
                        response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException | ParseException ex) {
            throw exceptionFactory.failure("Can't execute request.", ex);
        } finally {
            try {
                httpClient.close();
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't close request.", ex);
            }
        }
    }

    /**
     *
     * @return Must be closed after use.
     */
    private CloseableHttpClient getNonAuthHttpClient() {
        return HttpClients.custom().build();
    }

}
