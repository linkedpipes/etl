package com.linkedpipes.plugin.loader.lodCloud;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.linkedpipes.plugin.loader.lodCloud.LodCloudVocabulary.VCARD_FN;
import static com.linkedpipes.plugin.loader.lodCloud.LodCloudVocabulary.VCARD_HAS_EMAIL;

@SuppressWarnings("PackageAccessibility")
public final class LodCloud implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(LodCloud.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "Metadata")
    public SingleGraphDataUnit metadata;

    @Component.InputPort(iri = "Codelists")
    public SingleGraphDataUnit codelists;

    @Component.Configuration
    public LodCloudConfiguration configuration;

    @Override
    public void execute() throws LpException {

        LOG.debug("Querying metadata");

        String datasetUrl = executeSimpleSelectQuery("SELECT ?d WHERE {?d a <" + DCAT.DATASET + ">}", "d");
        String apiURI = configuration.getApiUri();
        String datasetID = configuration.getDatasetID();

        List<Map<String, Value>> distributions = executeSelectQuery("SELECT ?distribution WHERE {<" + datasetUrl + "> <"+ DCAT.HAS_DISTRIBUTION + "> ?distribution . ?distribution <" + LodCloudVocabulary.VOID_SPARQLENDPOINT + "> [] .  }");

        if (distributions.size() != 1) {
            throw new LpException("Expected 1 distribution with SPARQL endpoint. Found: " + distributions.size());
        }

        String title = executeSimpleSelectQuery("SELECT ?title WHERE {<" + datasetUrl + "> <"+ DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"en\"))}", "title");
        String description = executeSimpleSelectQuery("SELECT ?description WHERE {<" + datasetUrl + "> <"+ DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"en\"))}", "description");
        String authorName = executeSimpleSelectQuery("SELECT ?authorName WHERE {<" + datasetUrl + "> <"+ DCTERMS.PUBLISHER + ">/<" + FOAF.NAME  + "> ?authorName}", "authorName");
        String maintainerName = executeSimpleSelectQuery("SELECT ?maintainerName WHERE {<" + datasetUrl + "> <"+ DCAT.CONTACT_POINT + ">/<" + VCARD_FN  + "> ?maintainerName}", "maintainerName");
        String maintainerEmail = executeSimpleSelectQuery("SELECT ?maintainerEmail WHERE {<" + datasetUrl + "> <"+ DCAT.CONTACT_POINT + ">/<" + VCARD_HAS_EMAIL  + "> ?maintainerEmail}", "maintainerEmail");

        String distribution = distributions.get(0).get("distribution").stringValue();
        String dtitle = executeSimpleSelectQuery("SELECT ?title WHERE {<" + distribution + "> <"+ DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"en\"))}", "title");
        String ddescription = executeSimpleSelectQuery("SELECT ?description WHERE {<" + distribution + "> <"+ DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"en\"))}", "description");
        String dissued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + distribution + "> <"+ DCTERMS.ISSUED + "> ?issued .}", "issued");
        String dmodified = executeSimpleSelectQuery("SELECT ?modified WHERE {<" + distribution + "> <"+ DCTERMS.MODIFIED + "> ?modified .}", "modified");
        String sparqlEndpointVoid = executeSimpleSelectQuery("SELECT ?sparqlEndpoint WHERE {<" + distribution + "> <"+ LodCloudVocabulary.VOID_SPARQLENDPOINT + "> ?sparqlEndpoint }", "sparqlEndpoint");
        String datadump = executeSimpleSelectQuery("SELECT ?dwnld WHERE {<" + distribution + "> <"+ LodCloudVocabulary.VOID_DATADUMP + "> ?dwnld }", "dwnld");
        String triplecount = executeSimpleSelectQuery("SELECT ?triplecount WHERE {<" + distribution + "> <"+ LodCloudVocabulary.VOID_TRIPLES + "> ?triplecount }", "triplecount");
        String dformat = executeSimpleSelectQuery("SELECT ?format WHERE {<" + distribution + "> <"+ DCTERMS.FORMAT + "> ?format }", "format");
        String formatlabel = null;
        if (!dformat.isEmpty() && codelists != null) {
            formatlabel = executeSimpleCodelistSelectQuery("SELECT ?formatlabel WHERE {<" + dformat + "> <"+ SKOS.PREF_LABEL + "> ?formatlabel FILTER(LANGMATCHES(LANG(?formatlabel), \"en\"))}", "formatlabel");
        }
        String dmimetype = executeSimpleSelectQuery("SELECT ?mimetype WHERE {<" + distribution + "> <"+ DCAT.MEDIA_TYPE + "> ?mimetype }", "mimetype");
        String dlicense = executeSimpleSelectQuery("SELECT ?license WHERE {<" + distribution + "> <"+ DCTERMS.LICENSE + "> ?license }", "license");
        String dschema = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + distribution + "> <"+ DCTERMS.CONFORMS_TO + "> ?schema }", "schema");

        LinkedList<String> examples = new LinkedList<>();
        for (Map<String,Value> map: executeSelectQuery("SELECT ?exampleResource WHERE {<" + distribution + "> <"+ LodCloudVocabulary.VOID_EXAMPLERESOURCE + "> ?exampleResource }")) {
            examples.add(map.get("exampleResource").stringValue());
        }

        LOG.debug("Querying for the dataset in CKAN");
        boolean exists = false;
        Map<String, String> resUrlIdMap = new HashMap<>();
        Map<String, String> resFormatIdMap = new HashMap<>();

        CloseableHttpClient queryClient = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy()).build();
        HttpGet httpGet = new HttpGet(apiURI + "/package_show?id=" + datasetID);
        CloseableHttpResponse queryResponse = null;
        try {
            queryResponse = queryClient.execute(httpGet);
            if (queryResponse.getStatusLine().getStatusCode() == 200) {
                LOG.info("Dataset found");
                exists = true;

                JSONObject response =
                        new JSONObject(EntityUtils.toString(queryResponse.getEntity()))
                                .getJSONObject("result");
                JSONArray resourcesArray = response.getJSONArray("resources");
                for (int i = 0; i < resourcesArray.length(); i++ )
                {
                    try {
                        String id = resourcesArray.getJSONObject(i).getString("id");
                        String url = resourcesArray.getJSONObject(i).getString("url");
                        resUrlIdMap.put(url, id);

                        if (resourcesArray.getJSONObject(i).has("format")) {
                            String format = resourcesArray.getJSONObject(i).getString("format");
                            resFormatIdMap.put(format, id);
                        }

                    } catch (JSONException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }

            } else {
                //String ent = EntityUtils.toString(queryResponse.getEntity());
                LOG.info("Dataset not found");
            }
        } catch (IOException | ParseException | JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            if (queryResponse != null) {
                try {
                    queryResponse.close();
                    queryClient.close();
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        LOG.debug("Creating JSON");
        try {
            JSONObject root = new JSONObject();

            JSONArray tags = new JSONArray();
            tags.put(new JSONObject().put("name", "lod"));
            tags.put(new JSONObject().put("name", configuration.getVocabTag().toString()));
            tags.put(new JSONObject().put("name", configuration.getVocabMappingTag().toString()));
            tags.put(new JSONObject().put("name", configuration.getPublishedTag().toString()));
            tags.put(new JSONObject().put("name", configuration.getProvenanceMetadataTag().toString()));
            tags.put(new JSONObject().put("name", configuration.getLicenseMetadataTag().toString()));
            if (configuration.isLimitedSparql()) {
                tags.put(new JSONObject().put("name", "limited-sparql-endpoint"));
            }
            if (configuration.isLodcloudNolinks()) {
                tags.put(new JSONObject().put("name", "lodcloud.nolinks"));
            }
            if (configuration.isLodcloudUnconnected()) {
                tags.put(new JSONObject().put("name", "lodcloud.unconnected"));
            }
            if (configuration.isLodcloudNeedsInfo()) {
                tags.put(new JSONObject().put("name", "lodcloud.needsinfo"));
            }
            if (configuration.isLodcloudNeedsFixing()) {
                tags.put(new JSONObject().put("name", "lodcloud.needsfixing"));
            }
            for (String prefix : configuration.getVocabularies()) {
                tags.put(new JSONObject().put("name", "format-" + prefix));
            }
            tags.put(new JSONObject().put("name", configuration.getTopic()));
            for (String s : configuration.getAdditionalTags()) tags.put(new JSONObject().put("name", s));

            JSONArray resources = new JSONArray();

            // Start of Sparql Endpoint resource
            JSONObject sparqlEndpoint = new JSONObject();

            sparqlEndpoint.put("format","api/sparql");
            sparqlEndpoint.put("resource_type","api");
            sparqlEndpoint.put("description", configuration.getSparqlEndpointDescription());
            sparqlEndpoint.put("last_modified", dmodified);
            if (configuration.getSparqlEndpointName() == null || configuration.getSparqlEndpointName().isEmpty()) {
                sparqlEndpoint.put("name", "SPARQL endpoint");
            } else {
                sparqlEndpoint.put("name", configuration.getSparqlEndpointName());
            }
            sparqlEndpoint.put("url", sparqlEndpointVoid);

            if (resFormatIdMap.containsKey("api/sparql")) {
                sparqlEndpoint.put("id", resFormatIdMap.get("api/sparql"));
            }

            resources.put(sparqlEndpoint);
            // End of Sparql Endpoint resource

            // Start of VoID resource
            JSONObject voidJson = new JSONObject();

            voidJson.put("format","meta/void");
            voidJson.put("resource_type","file");
            voidJson.put("description","VoID description generated live");
            voidJson.put("name","VoID");
            voidJson.put("last_modified", dmodified);
            String voidUrl = sparqlEndpointVoid + "?query="
                    + URLEncoder.encode("DESCRIBE <" + distribution + ">", "UTF-8")
                    + "&output=" + URLEncoder.encode("text/turtle","UTF-8");
            voidJson.put("url", voidUrl);

            if (resFormatIdMap.containsKey("meta/void")) voidJson.put("id", resFormatIdMap.get("meta/void"));

            resources.put(voidJson);
            // End of VoID resource

            if (configuration.getVocabTag() != LodCloudConfiguration.VocabTags.NoProprietaryVocab
                    && !dschema.isEmpty()) {
                // Start of RDFS/OWL schema resource
                JSONObject schemaResource = new JSONObject();

                schemaResource.put("format","meta/rdf-schema");
                schemaResource.put("resource_type","file");
                schemaResource.put("description","RDFS/OWL Schema with proprietary vocabulary");
                schemaResource.put("name","RDFS/OWL schema");
                schemaResource.put("url", dschema );
                schemaResource.put("last_modified", dmodified);

                if (resFormatIdMap.containsKey("meta/rdf-schema")) {
                    schemaResource.put("id", resFormatIdMap.get("meta/rdf-schema"));
                }

                resources.put(schemaResource);
                // End of RDFS/OWL schema resource
            }

            // Start of Dump resource
            JSONObject dump = new JSONObject();

            dump.put("format", formatlabel);
            dump.put("mimetype", dmimetype.replaceAll(".*/([^/]+/[^/]+)","$1"));
            dump.put("resource_type","file");
            dump.put("name", dtitle);
            dump.put("description", ddescription);
            dump.put("created", dissued);
            dump.put("last_modified", dmodified);
            dump.put("url", datadump );

            if (resUrlIdMap.containsKey(datadump)) dump.put("id", resUrlIdMap.get(datadump));

            resources.put(dump);
            // End of Dump resource

            for (String example: examples)
            {
                // Start of Example resource text/turtle
                JSONObject exTurtle = new JSONObject();

                exTurtle.put("format","example/turtle");
                exTurtle.put("resource_type","file");
                //exTurtle.put("description","Generated by Virtuoso FCT");
                exTurtle.put("name","Example resource in Turtle");

                String exUrl;
                try {
                    if (sparqlEndpointVoid.isEmpty()) exUrl = example;
                    else exUrl = sparqlEndpointVoid
                            + "?query=" + URLEncoder.encode("DESCRIBE <", "UTF-8")
                            + example
                            + URLEncoder.encode(">", "UTF-8")
                            + "&default-graph-uri="
                            + URLEncoder.encode(datasetUrl,"UTF-8")
                            + "&output="
                            + URLEncoder.encode("text/turtle","UTF-8");
                } catch (UnsupportedEncodingException e) {
                    exUrl = "";
                    LOG.error(e.getLocalizedMessage(), e);
                }
                exTurtle.put("url", exUrl);

                if (resUrlIdMap.containsKey(exUrl)) exTurtle.put("id", resUrlIdMap.get(exUrl));

                resources.put(exTurtle);
                // End of text/turtle resource

                // Start of Example resource html
                JSONObject exHTML = new JSONObject();

                exHTML.put("format","HTML");
                exHTML.put("mimetype","text/html");
                exHTML.put("resource_type","file");
                exHTML.put("description","Generated by Virtuoso FCT");
                exHTML.put("name","Example resource in Virtuoso FCT");
                exHTML.put("last_modified", dmodified);
                exHTML.put("url", example );

                if (resUrlIdMap.containsKey(example)) exHTML.put("id", resUrlIdMap.get(example));

                resources.put(exHTML);
                // End of html resource

                // Mapping file resources
                for (LodCloudConfiguration.MappingFile mapping: configuration.getMappingFiles()) {
                    JSONObject exMapping = new JSONObject();

                    String mappingMime = "mapping/" + mapping.getMappingFormat();
                    exMapping.put("format",mappingMime);
                    exMapping.put("resource_type","file");
                    exMapping.put("description","Schema mapping file in " + mapping.getMappingFormat() + " format.");
                    exMapping.put("name","Mapping " + mapping.getMappingFormat());
                    exMapping.put("url", mapping.getMappingFile() );

                    if (resFormatIdMap.containsKey(mappingMime)) exMapping.put("id", resFormatIdMap.get(mappingMime));

                    resources.put(exMapping);
                }
                // End of mapping file resources

            }

            JSONArray extras = new JSONArray();
            extras.put(new JSONObject().put("key", "triples").put("value", triplecount));
            if (configuration.getShortname() != null && !configuration.getShortname().isEmpty()) {
                extras.put(
                        new JSONObject()
                                .put("key", "shortname")
                                .put("value", configuration.getShortname())
                );
            }
            if (configuration.getNamespace() != null && !configuration.getNamespace().isEmpty()) {
                extras.put(
                        new JSONObject()
                                .put("key", "namespace")
                                .put("value", configuration.getNamespace())
                );
            }
            if (!dlicense.isEmpty()) extras.put(
                    new JSONObject()
                            .put("key", "license_link")
                            .put("value", dlicense)
            );
            extras.put(
                    new JSONObject()
                            .put("key", "sparql_graph_name")
                            .put("value", datasetUrl)
            );
            for (LodCloudConfiguration.LinkCount link: configuration.getLinks()) {
                extras.put(
                        new JSONObject()
                                .put("key", "links:" + link.getTargetDataset())
                                .put("value", link.getLinkCount()));
            }

            if (configuration.getDatasetID() != null && !configuration.getDatasetID().isEmpty()) {
                root.put("name", configuration.getDatasetID());
            }
            root.put("url", datasetUrl);
            root.put("title", title);
            if (!maintainerName.isEmpty()) {
                root.put("maintainer", maintainerName);
            }
            if (!maintainerEmail.isEmpty()) {
                root.put("maintainer_email", maintainerEmail);
            }
            if (!authorName.isEmpty()) {
                root.put("author", authorName);
            }
            if (!maintainerEmail.isEmpty()) {
                root.put("author_email", maintainerEmail);
            }

            root.put("license_id", configuration.getLicense_id());
            root.put("notes", description);

            if (configuration.isVersionGenerated()) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date versiondate = new Date();
                String version = dateFormat.format(versiondate);
                root.put("version", version);
            }
            else if (configuration.getVersion() != null && !configuration.getVersion().isEmpty()) {
                root.put("version", configuration.getVersion());
            }

            root.put("tags", tags);
            root.put("resources", resources);
            root.put("extras", extras);

            if (!exists) {
                JSONObject createRoot = new JSONObject();

                createRoot.put("name", configuration.getDatasetID());
                createRoot.put("title", title);
                createRoot.put("owner_org", configuration.getOrgID());

                LOG.debug("Creating dataset in CKAN");
                CloseableHttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
                HttpPost httpPost = new HttpPost(apiURI + "/package_create?id=" + datasetID);
                httpPost.addHeader(new BasicHeader("Authorization", configuration.getApiKey()));

                String json = createRoot.toString();

                LOG.debug("Creating dataset with: " + json);

                httpPost.setEntity(new StringEntity(json, Charset.forName("utf-8")));

                CloseableHttpResponse response = null;

                try {
                    response = client.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        LOG.info("Dataset created OK: " + response.getStatusLine());
                    } else if (response.getStatusLine().getStatusCode() == 409) {
                        LOG.error("Dataset already exists: " + response.getStatusLine());
                        throw new LpException("Dataset already exists or cannot be created", "Dataset already exists or cannot be created: {0}", response.getStatusLine());
                    } else {
                        throw new LpException("Error while creating dataset", "Response while creating dataset: " + response.getStatusLine());
                    }
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                } finally {
                    if (response != null) {
                        try {
                            response.close();
                            client.close();
                        } catch (IOException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                            throw new LpException("Error creating dataset", e.getLocalizedMessage());
                        }
                    }
                }
            }

            LOG.debug("Posting to CKAN");
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(apiURI + "/package_update?id=" + datasetID);
            httpPost.addHeader(new BasicHeader("Authorization", configuration.getApiKey()));

            String json = root.toString();
            LOG.trace(json);

            httpPost.setEntity(new StringEntity(json, Charset.forName("utf-8")));

            CloseableHttpResponse response = null;

            try {
                response = client.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 200) {
                    LOG.info("Response: " + EntityUtils.toString(response.getEntity()));
                } else {
                    throw new LpException("Error updating dataset", "Response while updating dataset: {0}", response.getStatusLine());
                }
            } catch (IOException e) {
                LOG.error(e.getLocalizedMessage(), e);
            } finally {
                if (response != null) {
                    try {
                        response.close();
                        client.close();
                    } catch (IOException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        throw new LpException("Error updating dataset", e.getLocalizedMessage());
                    }
                }
            }
        } catch (JSONException | UnsupportedEncodingException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    private String executeSimpleSelectQuery(final String queryAsString, String bindingName) throws LpException {
        return metadata.execute((connection) -> {
            final TupleQuery preparedQuery = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, queryAsString);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(metadata.getReadGraph());
            preparedQuery.setDataset(dataset);
            //
            final BindingSet binding = QueryResults.singleResult(preparedQuery.evaluate());
            if (binding == null) {
                return "";
            } else {
                return binding.getValue(bindingName).stringValue();
            }
        });
    }

    private List<Map<String, Value>> executeSelectQuery(final String queryAsString) throws LpException {
        return metadata.execute((connection) -> {
            final List<Map<String, Value>> output = new LinkedList<>();
            final TupleQuery preparedQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryAsString);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(metadata.getReadGraph());
            preparedQuery.setDataset(dataset);
            //
            TupleQueryResult result = preparedQuery.evaluate();
            while (result.hasNext()) {
                final BindingSet binding = result.next();
                final Map<String, Value> row = new HashMap<>();
                binding.forEach((item) -> {
                    row.put(item.getName(), item.getValue());
                });
                output.add(row);
            }

            return output;
        });
    }

    private String executeSimpleCodelistSelectQuery(final String queryAsString, String bindingName) throws LpException {
        return codelists.execute((connection) -> {
            final TupleQuery preparedQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryAsString);
            final SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(codelists.getReadGraph());
            preparedQuery.setDataset(dataset);
            //
            final BindingSet binding = QueryResults.singleResult(preparedQuery.evaluate());
            if (binding == null) {
                return "";
            } else {
                return binding.getValue(bindingName).stringValue();
            }
        });
    }


}
