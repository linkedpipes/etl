package com.linkedpipes.plugin.loader.dcatApToCkan;

import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.apache.commons.io.FileUtils;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
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
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class DcatApToCkan implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(DcatApToCkan.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "Metadata")
    public SingleGraphDataUnit metadata;

    @Component.OutputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outFileSimple;

    @Component.Configuration
    public DcatApToCkanConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        // Load files.
        LOG.debug("Querying metadata");

        String datasetID = configuration.getDatasetID();
        String orgID = configuration.getOrgID();
        String apiURI = configuration.getApiUri();

        String datasetURI = executeSimpleSelectQuery("SELECT ?d WHERE {?d a <" + DcatApToCkanVocabulary.DCAT_DATASET_CLASS + ">}", "d");
        String title = executeSimpleSelectQuery("SELECT ?title WHERE {<" + datasetURI + "> <" + DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"" + configuration.getLoadLanguage() + "\"))}", "title");
        String description = executeSimpleSelectQuery("SELECT ?description WHERE {<" + datasetURI + "> <" + DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"" + configuration.getLoadLanguage() + "\"))}", "description");
        String periodicity = executeSimpleSelectQuery("SELECT ?periodicity WHERE {<" + datasetURI + "> <" + DCTERMS.ACCRUAL_PERIODICITY + ">/<" + DCTERMS.TITLE + "> ?periodicity }", "periodicity");
        String temporalStart = executeSimpleSelectQuery("SELECT ?temporalStart WHERE {<" + datasetURI + "> <" + DCTERMS.TEMPORAL + ">/<" + DcatApToCkanVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
        String temporalEnd = executeSimpleSelectQuery("SELECT ?temporalEnd WHERE {<" + datasetURI + "> <" + DCTERMS.TEMPORAL + ">/<" + DcatApToCkanVocabulary.SCHEMA_ENDDATE + "> ?temporalEnd }", "temporalEnd");
        String spatial = executeSimpleSelectQuery("SELECT ?spatial WHERE {<" + datasetURI + "> <" + DCTERMS.SPATIAL + "> ?spatial }", "spatial");
        String schemaURL = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + datasetURI + "> <" + DCTERMS.REFERENCES + "> ?schema }", "schema");
        String curatorName = executeSimpleSelectQuery("SELECT ?name WHERE {<" + datasetURI + "> <" + DcatApToCkanVocabulary.DCAT_CONTACT_POINT + ">/<" + DcatApToCkanVocabulary.VCARD_FN + "> ?name }", "name");
        String contactPoint = executeSimpleSelectQuery("SELECT ?contact WHERE {<" + datasetURI + "> <" + DcatApToCkanVocabulary.DCAT_CONTACT_POINT + ">/<" + DcatApToCkanVocabulary.VCARD_HAS_EMAIL + "> ?contact }", "contact");
        String issued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + datasetURI + "> <" + DCTERMS.ISSUED + "> ?issued }", "issued");
        String modified = executeSimpleSelectQuery("SELECT ?modified WHERE {<" + datasetURI + "> <" + DCTERMS.MODIFIED + "> ?modified }", "modified");
        String license = executeSimpleSelectQuery("SELECT ?license WHERE {<" + datasetURI + "> <" + DCTERMS.LICENSE + "> ?license }", "license");
        String publisher_uri = executeSimpleSelectQuery("SELECT ?publisher_uri WHERE {<" + datasetURI + "> <" + DCTERMS.PUBLISHER + "> ?publisher_uri }", "publisher_uri");
        String publisher_name = executeSimpleSelectQuery("SELECT ?publisher_name WHERE {<" + datasetURI + "> <" + DCTERMS.PUBLISHER + ">/<" + FOAF.NAME + "> ?publisher_name }", "publisher_name");

        LinkedList<String> themes = new LinkedList<String>();
        for (Map<String, Value> map : executeSelectQuery("SELECT ?theme WHERE {<" + datasetURI + "> <" + DcatApToCkanVocabulary.DCAT_THEME + "> ?theme }")) {
            themes.add(map.get("theme").stringValue());
        }

        LinkedList<String> keywords = new LinkedList<String>();
        for (Map<String, Value> map : executeSelectQuery("SELECT ?keyword WHERE {<" + datasetURI + "> <" + DcatApToCkanVocabulary.DCAT_KEYWORD + "> ?keyword FILTER(LANGMATCHES(LANG(?keyword), \"" + configuration.getLoadLanguage() + "\"))}")) {
            keywords.add(map.get("keyword").stringValue());
        }

        LinkedList<String> distributions = new LinkedList<String>();
        for (Map<String, Value> map : executeSelectQuery("SELECT ?distribution WHERE {<" + datasetURI + "> <" + DcatApToCkanVocabulary.DCAT_DISTRIBUTION + "> ?distribution }")) {
            distributions.add(map.get("distribution").stringValue());
        }

        boolean exists = false;
        Map<String, String> resUrlIdMap = new HashMap<String, String>();
        Map<String, String> resDistroIdMap = new HashMap<String, String>();
        Map<String, JSONObject> resourceList = new HashMap<String, JSONObject>();

        LOG.debug("Querying for the dataset in CKAN");
        CloseableHttpClient queryClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        HttpGet httpGet = new HttpGet(apiURI + "/package_show?id=" + datasetID);
        CloseableHttpResponse queryResponse = null;
        try {
            queryResponse = queryClient.execute(httpGet);
            if (queryResponse.getStatusLine().getStatusCode() == 200) {
                LOG.info("Dataset found");
                exists = true;

                if (!configuration.isOverwrite()) {
                    JSONObject response = new JSONObject(EntityUtils.toString(queryResponse.getEntity())).getJSONObject("result");
                    JSONArray resourcesArray = response.getJSONArray("resources");
                    for (int i = 0; i < resourcesArray.length(); i++) {
                        try {
                            String id = resourcesArray.getJSONObject(i).getString("id");
                            resourceList.put(id, resourcesArray.getJSONObject(i));

                            String url = resourcesArray.getJSONObject(i).getString("url");
                            resUrlIdMap.put(url, id);

                            if (resourcesArray.getJSONObject(i).has("distro_url")) {
                                String distro = resourcesArray.getJSONObject(i).getString("distro_url");
                                resDistroIdMap.put(distro, id);
                            }
                        } catch (JSONException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                }

            } else {
                String ent = EntityUtils.toString(queryResponse.getEntity());
                LOG.info("Dataset not found: " + ent);
            }
        } catch (ClientProtocolException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } catch (ParseException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } catch (JSONException e) {
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
            //tags.put(keywords);
            for (String keyword : keywords) {
                tags.put(new JSONObject().put("name", keyword));
            }

            JSONArray resources = new JSONArray();

            //JSONObject extras = new JSONObject();
            if (!datasetID.isEmpty()) {
                root.put("name", datasetID);
            }
            if (!title.isEmpty()) {
                root.put("title", title);
            }
            if (!description.isEmpty()) {
                root.put("notes", description);
            }
            if (!contactPoint.isEmpty()) {
                root.put("maintainer_email", contactPoint);
            }
            if (!curatorName.isEmpty()) {
                root.put("maintainer", curatorName);
            }
            if (!issued.isEmpty()) {
                root.put("metadata_created", issued);
            }
            if (!modified.isEmpty()) {
                root.put("metadata_modified", modified);
            }
            if (!publisher_uri.isEmpty()) {
                root.put("publisher_uri", publisher_uri);
            }
            if (!publisher_name.isEmpty()) {
                root.put("publisher_name", publisher_name);
            }

            //TODO: Matching?
            root.put("license_id", "other-open");
            root.put("license_link", license);

            if (!temporalStart.isEmpty()) {
                root.put("temporal_start", temporalStart);
            }
            if (!temporalEnd.isEmpty()) {
                root.put("temporal_end", temporalEnd);
            }
            if (!periodicity.isEmpty()) {
                root.put("frequency", periodicity);
            }
            if (!schemaURL.isEmpty()) {
                root.put("schema", schemaURL);
            }
            if (!spatial.isEmpty()) {
                root.put("ruian_type", "ST");
                root.put("ruian_code", 1);
                root.put("spatial_uri", spatial);
            }

            String concatThemes = "";
            for (String theme : themes) {
                concatThemes += theme + " ";
            }
            if (!concatThemes.isEmpty()) {
                root.put("theme", concatThemes);
            }

            //Distributions
            for (String distribution : distributions) {
                JSONObject distro = new JSONObject();

                String dtitle = executeSimpleSelectQuery("SELECT ?title WHERE {<" + distribution + "> <" + DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"" + configuration.getLoadLanguage() + "\"))}", "title");
                String ddescription = executeSimpleSelectQuery("SELECT ?description WHERE {<" + distribution + "> <" + DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"" + configuration.getLoadLanguage() + "\"))}", "description");
                String dtemporalStart = executeSimpleSelectQuery("SELECT ?temporalStart WHERE {<" + distribution + "> <" + DCTERMS.TEMPORAL + ">/<" + DcatApToCkanVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
                String dtemporalEnd = executeSimpleSelectQuery("SELECT ?temporalEnd WHERE {<" + distribution + "> <" + DCTERMS.TEMPORAL + ">/<" + DcatApToCkanVocabulary.SCHEMA_ENDDATE + "> ?temporalEnd }", "temporalEnd");
//		    	String dspatial = executeSimpleSelectQuery("SELECT ?spatial WHERE {<" + distribution + "> <"+ DCTERMS.SPATIAL + "> ?spatial }", "spatial");
                String dschemaURL = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + distribution + "> <" + DcatApToCkanVocabulary.WDRS_DESCRIBEDBY + "> ?schema }", "schema");
                String dschemaType = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + distribution + "> <" + DcatApToCkanVocabulary.POD_DISTRIBUTION_DESCRIBREBYTYPE + "> ?schema }", "schema");
                String dissued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + distribution + "> <" + DCTERMS.ISSUED + "> ?issued }", "issued");
                String dmodified = executeSimpleSelectQuery("SELECT ?modified WHERE {<" + distribution + "> <" + DCTERMS.MODIFIED + "> ?modified }", "modified");
                String dlicense = executeSimpleSelectQuery("SELECT ?license WHERE {<" + distribution + "> <" + DCTERMS.LICENSE + "> ?license }", "license");
                String dformat = executeSimpleSelectQuery("SELECT ?format WHERE {<" + distribution + "> <" + DCTERMS.FORMAT + ">/<" + DCTERMS.TITLE + "> ?format }", "format");
                String dwnld = executeSimpleSelectQuery("SELECT ?dwnld WHERE {<" + distribution + "> <" + DcatApToCkanVocabulary.DCAT_DOWNLOADURL + "> ?dwnld }", "dwnld");

                // RDF SPECIFIC - VOID
                String sparqlEndpoint = executeSimpleSelectQuery("SELECT ?sparqlEndpoint WHERE {<" + distribution + "> <" + DcatApToCkanVocabulary.VOID_SPARQLENDPOINT + "> ?sparqlEndpoint }", "sparqlEndpoint");

                LinkedList<String> examples = new LinkedList<String>();
                for (Map<String, Value> map : executeSelectQuery("SELECT ?exampleResource WHERE {<" + distribution + "> <" + DcatApToCkanVocabulary.VOID_EXAMPLERESOURCE + "> ?exampleResource }")) {
                    examples.add(map.get("exampleResource").stringValue());
                }

                if (!sparqlEndpoint.isEmpty()) {
                    //Start of Sparql Endpoint resource
                    JSONObject sparqlEndpointJSON = new JSONObject();

                    sparqlEndpointJSON.put("name", "SPARQL Endpoint");
                    sparqlEndpointJSON.put("url", sparqlEndpoint);
                    sparqlEndpointJSON.put("format", "api/sparql");
                    sparqlEndpointJSON.put("mimetype", "text/turtle");
                    sparqlEndpointJSON.put("resource_type", "api");
                    if (!dissued.isEmpty()) {
                        sparqlEndpointJSON.put("created", dissued);
                    }
                    if (!dmodified.isEmpty()) {
                        sparqlEndpointJSON.put("last_modified", dmodified);
                    }
                    if (!dlicense.isEmpty()) {
                        sparqlEndpointJSON.put("license_link", dlicense);
                    }
                    if (!dtemporalStart.isEmpty()) {
                        sparqlEndpointJSON.put("temporal_start", dtemporalStart);
                    }
                    if (!dtemporalEnd.isEmpty()) {
                        sparqlEndpointJSON.put("temporal_end", dtemporalEnd);
                    }
                    if (!dschemaURL.isEmpty()) {
                        sparqlEndpointJSON.put("describedBy", dschemaURL);
                    }
                    if (!dschemaType.isEmpty()) {
                        sparqlEndpointJSON.put("describedByType", dschemaType);
                    }

                    if (resUrlIdMap.containsKey(sparqlEndpoint)) {
                        String id = resUrlIdMap.get(sparqlEndpoint);
                        sparqlEndpointJSON.put("id", id);
                        resourceList.remove(id);
                    }

                    resources.put(sparqlEndpointJSON);
                    // End of Sparql Endpoint resource

                }

                for (String example : examples) {
                    if (configuration.isGenerateVirtuosoTurtleExampleResource()) {
                        // Start of Example resource text/turtle
                        JSONObject exTurtle = new JSONObject();

                        exTurtle.put("format", "example/turtle");
                        exTurtle.put("mimetype", "text/turtle");
                        exTurtle.put("resource_type", "file");
                        //exTurtle.put("description","Generated by Virtuoso FCT");
                        exTurtle.put("name", "Example resource in Turtle");
                        if (!dissued.isEmpty()) {
                            exTurtle.put("created", dissued);
                        }
                        if (!dmodified.isEmpty()) {
                            exTurtle.put("last_modified", dmodified);
                        }

                        String exUrl;

                        try {
                            if (sparqlEndpoint.isEmpty()) {
                                exUrl = example;
                            } else {
                                exUrl = sparqlEndpoint + "?query=" + URLEncoder.encode("DESCRIBE <", "UTF-8") + example + URLEncoder.encode(">", "UTF-8")
                                        + "&default-graph-uri=" + URLEncoder.encode(datasetURI, "UTF-8")
                                        + "&output=" + URLEncoder.encode("text/turtle", "UTF-8");
                            }
                        } catch (UnsupportedEncodingException e) {
                            exUrl = "";
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                        exTurtle.put("url", exUrl);

                        if (resUrlIdMap.containsKey(exUrl)) {
                            String id = resUrlIdMap.get(exUrl);
                            exTurtle.put("id", id);
                            resourceList.remove(id);
                        }

                        resources.put(exTurtle);
                        // End of text/turtle resource
                    }

                    if (configuration.isGenerateExampleResource()) {
                        // Start of Example resource html
                        JSONObject exHTML = new JSONObject();

                        exHTML.put("format", "HTML");
                        exHTML.put("mimetype", "text/html");
                        exHTML.put("resource_type", "file");
                        //exHTML.put("description","Generated by Virtuoso FCT");
                        exHTML.put("name", "Example resource");
                        exHTML.put("url", example);
                        if (!dissued.isEmpty()) {
                            exHTML.put("created", dissued);
                        }
                        if (!dmodified.isEmpty()) {
                            exHTML.put("last_modified", dmodified);
                        }

                        if (resUrlIdMap.containsKey(example)) {
                            String id = resUrlIdMap.get(example);
                            exHTML.put("id", id);
                            resourceList.remove(id);
                        }

                        resources.put(exHTML);
                        // End of html resource
                    }

                }

                // END OF RDF VOID SPECIFICS
                if (!dtitle.isEmpty()) {
                    distro.put("name", dtitle);
                }
                if (!ddescription.isEmpty()) {
                    distro.put("description", ddescription);
                }
                if (!dlicense.isEmpty()) {
                    distro.put("license_link", dlicense);
                }
                if (!dtemporalStart.isEmpty()) {
                    distro.put("temporal_start", dtemporalStart);
                }
                if (!dtemporalEnd.isEmpty()) {
                    distro.put("temporal_end", dtemporalEnd);
                }
                if (!dschemaURL.isEmpty()) {
                    distro.put("describedBy", dschemaURL);
                }
                if (!dschemaType.isEmpty()) {
                    distro.put("describedByType", dschemaType);
                }
                if (!dformat.isEmpty()) {
                    distro.put("format", dformat);
                }
                if (!dformat.isEmpty()) {
                    distro.put("mimetype", dformat);
                }
                if (!dwnld.isEmpty()) {
                    distro.put("url", dwnld);
                }
                if (!distribution.isEmpty()) {
                    distro.put("distro_url", distribution);
                }

                distro.put("resource_type", "file");

                if (resDistroIdMap.containsKey(distribution)) {
                    String id = resDistroIdMap.get(distribution);
                    distro.put("id", id);
                    resourceList.remove(id);
                } else if (resUrlIdMap.containsKey(dwnld)) {
                    String id = resUrlIdMap.get(dwnld);
                    distro.put("id", id);
                    resourceList.remove(id);
                }

                if (!dissued.isEmpty()) {
                    distro.put("created", dissued);
                }
                if (!dmodified.isEmpty()) {
                    distro.put("last_modified", dmodified);
                }

//				if (!dspatial.isEmpty()) {
//					distro.put("ruian_type", "ST");
//					distro.put("ruian_code", 1);
//					distro.put("spatial_uri", dspatial);
//				}
                resources.put(distro);
            }

            //Add the remaining distributions that were not updated but existed in the original dataset
            for (Entry<String, JSONObject> resource : resourceList.entrySet()) {
                resources.put(resource.getValue());
            }

            root.put("tags", tags);
            root.put("resources", resources);
            //root.put("extras", extras);

            if (!exists && configuration.isLoadToCKAN()) {
                JSONObject createRoot = new JSONObject();

                createRoot.put("name", datasetID);
                createRoot.put("title", title);
                createRoot.put("owner_org", orgID);

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
                        LOG.info("Dataset created OK");
                        LOG.info("Response: " + EntityUtils.toString(response.getEntity()));
                    } else if (response.getStatusLine().getStatusCode() == 409) {
                        String ent = EntityUtils.toString(response.getEntity());
                        LOG.error("Dataset already exists: " + ent);
                        throw exceptionFactory.failure("Dataset already exists");
                        //ContextUtils.sendError(context, "Dataset already exists", "Dataset already exists: {0}: {1}", response.getStatusLine().getStatusCode(), ent);
                    } else {
                        String ent = EntityUtils.toString(response.getEntity());
                        LOG.error("Response:" + ent);
                        throw exceptionFactory.failure("Error creating dataset");
                        //ContextUtils.sendError(context, "Error creating dataset", "Response while creating dataset: {0}: {1}", response.getStatusLine().getStatusCode(), ent);
                    }
                } catch (ClientProtocolException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                } finally {
                    if (response != null) {
                        try {
                            response.close();
                            client.close();
                        } catch (IOException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                            throw exceptionFactory.failure("Error creating dataset");
                            //ContextUtils.sendError(context, "Error creating dataset", e.getLocalizedMessage());
                        }
                    }
                }
            }

            String json = root.toString();

            File outfile = outFileSimple.createFile(configuration.getFilename());
            try {
                FileUtils.writeStringToFile(outfile, json, "UTF-8");
            } catch (IOException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

            if (configuration.isLoadToCKAN()) {
                LOG.debug("Posting to CKAN");
                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(apiURI + "/package_update?id=" + datasetID);
                httpPost.addHeader(new BasicHeader("Authorization", configuration.getApiKey()));

                LOG.trace(json);

                httpPost.setEntity(new StringEntity(json, Charset.forName("utf-8")));

                CloseableHttpResponse response = null;

                try {
                    response = client.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        LOG.info("Response:" + EntityUtils.toString(response.getEntity()));
                    } else {
                        String ent = EntityUtils.toString(response.getEntity());
                        LOG.error("Response:" + ent);
                        throw exceptionFactory.failure("Error updating dataset");
                        //ContextUtils.sendError(context, "Error updating dataset", "Response while updating dataset: {0}: {1}", response.getStatusLine().getStatusCode(), ent);
                    }
                } catch (ClientProtocolException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                } finally {
                    if (response != null) {
                        try {
                            response.close();
                            client.close();
                        } catch (IOException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                            throw exceptionFactory.failure("Error updating dataset");
//		                	ContextUtils.sendError(context, "Error updating dataset", e.getLocalizedMessage());
                        }
                    }
                }
            }
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private String executeSimpleSelectQuery(final String queryAsString, String bindingName) throws LpException {
        return metadata.execute((connection) -> {
            final TupleQuery preparedQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryAsString);
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

}
