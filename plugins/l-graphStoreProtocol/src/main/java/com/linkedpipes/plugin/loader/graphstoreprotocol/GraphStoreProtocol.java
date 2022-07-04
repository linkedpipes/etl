package com.linkedpipes.plugin.loader.graphstoreprotocol;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.RdfToPojoLoader;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GraphStoreProtocol implements Component, SequentialExecution {

    private static Logger LOG
            = LoggerFactory.getLogger(GraphStoreProtocol.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.Configuration
    public GraphStoreProtocolConfiguration configuration;

    protected HttpService httpClient;

    @Override
    public void execute() throws LpException {
        checkConfiguration();
        httpClient = new HttpService(
                configuration.isUseAuthentication(),
                configuration.getUserName(),
                configuration.getPassword(),
                configuration.getEndpoint());
        Map<String, Entry> entries = loadEntries();
        for (FilesDataUnit.Entry entry : inputFiles) {
            executeEntry(entry, entries.get(entry.getFileName()));
        }
    }

    private void checkConfiguration() throws LpException {
        if (configuration.getEndpoint() == null
                || configuration.getEndpoint().isEmpty()) {
            throw new LpException("Missing property: {}",
                    GraphStoreProtocolVocabulary.HAS_CRUD);
        }
        if (inputFiles.size() > 1 && configuration.isReplace()) {
            throw new LpException("Only one file can be uploaded "
                    + "with replace mode.");
        }
    }

    private Map<String, Entry> loadEntries() throws RdfException {
        List<String> resources = inputRdf.asRdfSource().getByType(
                GraphStoreProtocolVocabulary.ENTRY);
        Map<String, Entry> result = new HashMap<>();
        for (String resource : resources) {
            Entry entry = new Entry();
            RdfToPojoLoader.loadByReflection(
                    inputRdf.asRdfSource(), resource, entry);
            result.put(entry.fileName, entry);
        }
        return result;
    }

    private void executeEntry(FilesDataUnit.Entry fileEntry, Entry entry)
            throws LpException {
        Optional<RDFFormat> rdfFormat
                = Rio.getParserFormatForFileName(fileEntry.getFileName());
        if (rdfFormat.isEmpty()) {
            throw new LpException(
                    "Can't determine format for file: {}", fileEntry);
        }
        if (rdfFormat.get().supportsContexts()) {
            throw new LpException(
                    "Quad-based formats are not supported.");
        }
        String graph;
        if (entry == null || isBlankString(entry.targetGraph)) {
            graph = configuration.getTargetGraph();
        } else {
            graph = entry.targetGraph;
        }
        Long beforeSize = null;
        if (configuration.isCheckSize()) {
            try {
                beforeSize = getGraphSize(graph);
            } catch (IOException ex) {
                throw new LpException("Can't get graph size.", ex);
            }
        }
        String mimeType = rdfFormat.get().getDefaultMIMEType();
        //
        LOG.debug("Using repository: {}", configuration.getRepository());
        switch (configuration.getRepository()) {
            case BLAZEGRAPH:
                uploadBlazegraph(configuration.getEndpoint(),
                        fileEntry.toFile(), mimeType, graph,
                        configuration.isReplace());
                break;
            case FUSEKI:
                uploadFuseki(configuration.getEndpoint(),
                        fileEntry.toFile(), mimeType, graph,
                        configuration.isReplace());
                break;
            case VIRTUOSO:
                uploadVirtuoso(configuration.getEndpoint(),
                        fileEntry.toFile(), mimeType, graph,
                        configuration.isReplace());
                break;
            case GRAPHDB:
                uploadGraphDb(configuration.getEndpoint(),
                        fileEntry.toFile(), mimeType, graph,
                        configuration.isReplace());
                break;
            default:
                throw new LpException("Unknown repository type!");
        }

        if (configuration.isCheckSize()) {
            long afterSize;
            try {
                afterSize = getGraphSize(graph);
            } catch (IOException ex) {
                throw new LpException("Can't get graph size.", ex);
            }
            LOG.info(
                    "Graph '{}' size changed from {} to {} by uploading '{}'.",
                    graph, beforeSize, afterSize, fileEntry.getFileName());
        }
    }

    boolean isBlankString(String string) {
        return string == null || string.trim().isEmpty();
    }

    protected long getGraphSize(String graph) throws LpException, IOException {
        try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
            long result = GraphSize.getGraphSize(
                    httpclient, configuration.getEndpointSelect(), graph);
            LOG.info("Graph '{}' size: {}", graph, result);
            return result;
        }
    }

    protected void uploadBlazegraph(
            String url, File file, String mimeType,
            String graph, boolean replace) throws LpException {
        LOG.info("Blazegraph: {} {} {} {} {}", url, file.getName(), mimeType,
                graph, replace);
        if (graph == null) {
            throw new LpException("Blazegraph require2 graph to be set!");
        }
        url += encodeForUrlQuery("?graph", graph);
        HttpEntityEnclosingRequestBase httpMethod;
        if (replace) {
            // Blaze graph delete statements based on provided query.
            String query = "CONSTRUCT{ ?s ?p ?o} FROM <" + graph
                    + "> WHERE { ?s ?p ?o }";
            url += encodeForUrlQuery("&query", query);
            //
            httpMethod = new HttpPut(url);
        } else {
            httpMethod = new HttpPost(url);
        }
        httpMethod.setEntity(
                new FileEntity(file, ContentType.create(mimeType)));
        //
        httpClient.executeHttp(httpMethod);
    }

    protected String encodeForUrlQuery(String prefix, String value) {
        if (value == null) {
            return "";
        }
        String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
        return prefix + "=" + encodedValue;
    }

    protected void uploadFuseki(
            String url, File file, String mimeType,
            String graph, boolean replace) throws LpException {
        LOG.info("Fuseki: {} {} {} {} {}", url, file.getName(), mimeType,
                graph, replace);
        //
        url += encodeForUrlQuery("?graph", graph);
        HttpEntityEnclosingRequestBase httpMethod;
        if (replace) {
            httpMethod = new HttpPut(url);
        } else {
            httpMethod = new HttpPost(url);
        }
        FileBody fileBody = new FileBody(file,
                ContentType.create(mimeType), "file");
        HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .build();
        httpMethod.setEntity(entity);
        //
        httpClient.executeHttp(httpMethod);
    }

    protected void uploadVirtuoso(
            String url, File file, String mimeType,
            String graph, boolean replace) throws LpException {
        LOG.info("Virtuoso: {} {} {} {} {}", url, file.getName(), mimeType,
                graph, replace);
        //
        url += encodeForUrlQuery("?graph", graph);
        HttpEntityEnclosingRequestBase httpMethod;
        if (replace) {
            httpMethod = new HttpPut(url);
        } else {
            httpMethod = new HttpPost(url);
        }
        //
        httpMethod.setEntity(new FileEntity(file,
                ContentType.create(mimeType)));
        //
        httpClient.executeHttp(httpMethod);
    }

    protected void uploadGraphDb(
            String url, File file, String mimeType,
            String graph, boolean replace) throws LpException {
        LOG.info("GraphDB: {} {} {} {} {}", url, file.getName(), mimeType,
                graph, replace);
        //
        url += encodeForUrlQuery("?graph", graph);
        HttpEntityEnclosingRequestBase httpMethod;
        if (replace) {
            httpMethod = new HttpPut(url);
        } else {
            httpMethod = new HttpPost(url);
        }
        ContentType contentType = ContentType.create(mimeType);
        httpMethod.setEntity(new FileEntity(file, contentType));
        //
        httpClient.executeHttp(httpMethod);
    }

}
