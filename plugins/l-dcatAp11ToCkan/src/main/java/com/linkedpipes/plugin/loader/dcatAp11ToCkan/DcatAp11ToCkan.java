package com.linkedpipes.plugin.loader.dcatAp11ToCkan;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
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
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class DcatAp11ToCkan implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(DcatAp11ToCkan.class);

    @Component.InputPort(iri = "Metadata")
    public SingleGraphDataUnit metadata;

    @Component.InputPort(iri = "Codelists")
    public SingleGraphDataUnit codelists;

    @Component.Configuration
    public DcatAp11ToCkanConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    private CloseableHttpClient queryClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
    private CloseableHttpClient createClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
    private CloseableHttpClient postClient = HttpClients.createDefault();

    private String apiURI;

    private String fixKeyword(String keyword) {
        return keyword.replace(",","")
                .replace(".","")
                .replace("/","-")
                .replace(":","-")
                .replace(";","-")
                .replace("§", "paragraf");
    }

    private Map<String, String> getOrganizations() {
        CloseableHttpResponse queryResponse = null;
        List<String> organizationList = new LinkedList<>();
        Map<String, String> organizations = new HashMap<>();
        HttpGet httpGetOrg = new HttpGet(apiURI + "/organization_list");

        LOG.debug("Querying CKAN for organizations");

        try {
            queryResponse = queryClient.execute(httpGetOrg);
            if (queryResponse.getStatusLine().getStatusCode() == 200) {
                JSONArray response = new JSONObject(EntityUtils.toString(queryResponse.getEntity())).getJSONArray("result");
                for (Object o : response) {
                    organizationList.add(o.toString());
                }
                LOG.info("Organization list downloaded, found " + organizationList.size() + " organizations.");

            } else {
                String ent = EntityUtils.toString(queryResponse.getEntity());
                LOG.info("Organizations not downloaded: " + ent);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            if (queryResponse != null) {
                try {
                    queryResponse.close();
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        LOG.debug("Querying for organization details.");

        for (String organization : organizationList) {
            HttpGet httpGetOrgDetail = new HttpGet(apiURI + "/organization_show?id=" + organization);
            try {
                queryResponse = queryClient.execute(httpGetOrgDetail);
                if (queryResponse.getStatusLine().getStatusCode() == 200) {
                    LOG.info("Organization " + organization + " downloaded");

                    JSONObject response = new JSONObject(EntityUtils.toString(queryResponse.getEntity())).getJSONObject("result");
                    JSONArray org_extras = response.getJSONArray("extras");
                    for (Object extra : org_extras) {
                        String extraKey = ((JSONObject) extra).getString("key");
                        String extraValue = ((JSONObject) extra).getString("value");
                        if (extraKey.equals("uri")) {
                            organizations.put(extraValue, organization);
                            break;
                        }
                    }
                } else {
                    String ent = EntityUtils.toString(queryResponse.getEntity());
                    LOG.info("Organization " + organization + " not downloaded: " + ent);
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            } finally {
                if (queryResponse != null) {
                    try {
                        queryResponse.close();
                    } catch (IOException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }

        return organizations;
    }

    @Override
    public void execute() throws LpException {

        apiURI = configuration.getApiUri();
        String datasetID = configuration.getDatasetID();

        if (datasetID == null || datasetID.isEmpty() || apiURI == null || apiURI.isEmpty() || configuration.getApiKey() == null || configuration.getApiKey().isEmpty() ) {
            throw exceptionFactory.failure("Missing required settings.");
        }

        Map<String, String> organizations = getOrganizations();

        LOG.debug("Querying metadata for datasets");

        LinkedList<String> datasets = new LinkedList<>();
        for (Map<String,Value> map: executeSelectQuery("SELECT ?d WHERE {?d a <" + DcatAp11ToCkanVocabulary.DCAT_DATASET_CLASS + ">}")) {
            datasets.add(map.get("d").stringValue());
        }

        int current = 0;
        int total = datasets.size();

        LOG.info("Found " + total + " datasets");

        progressReport.start(total);

        current++;

        String datasetURI = datasets.getFirst();

        CloseableHttpResponse queryResponse = null;

        LOG.info("Processing dataset " + current + "/" + total + ": " + datasetURI);

        boolean datasetExists = false;

        Map<String, String> resUrlIdMap = new HashMap<>();
        Map<String, String> resDistroIdMap = new HashMap<>();
        Map<String, JSONObject> resourceList = new HashMap<>();

        LOG.debug("Querying for the dataset " + datasetID + " in CKAN");
        HttpGet httpGet = new HttpGet(apiURI + "/package_show?id=" + datasetID);
        try {
            queryResponse = queryClient.execute(httpGet);
            if (queryResponse.getStatusLine().getStatusCode() == 200) {
                LOG.debug("Dataset found");
                datasetExists = true;

                JSONObject response = new JSONObject(EntityUtils.toString(queryResponse.getEntity())).getJSONObject("result");
                JSONArray resourcesArray = response.getJSONArray("resources");
                for (int i = 0; i < resourcesArray.length(); i++) {
                    String id = resourcesArray.getJSONObject(i).getString("id");
                    resourceList.put(id, resourcesArray.getJSONObject(i));

                    String url = resourcesArray.getJSONObject(i).getString("url");
                    resUrlIdMap.put(url, id);

                    if (resourcesArray.getJSONObject(i).has("distro_url")) {
                        String distro = resourcesArray.getJSONObject(i).getString("distro_url");
                        resDistroIdMap.put(distro, id);
                    }
                }
            } else {
                String ent = EntityUtils.toString(queryResponse.getEntity());
                LOG.debug("Dataset not found: " + ent);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            if (queryResponse != null) {
                try {
                    queryResponse.close();
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        LinkedList<String> keywords = new LinkedList<>();
        for (Map<String, Value> map : executeSelectQuery("SELECT ?keyword WHERE {<" + datasetURI + "> <" + DcatAp11ToCkanVocabulary.DCAT_KEYWORD + "> ?keyword FILTER(LANGMATCHES(LANG(?keyword), \"" + configuration.getLoadLanguage() + "\"))}")) {
            keywords.add(map.get("keyword").stringValue());
        }

        String publisher_uri = executeSimpleSelectQuery("SELECT ?publisher_uri WHERE {<" + datasetURI + "> <" + DCTERMS.PUBLISHER + "> ?publisher_uri }", "publisher_uri");
        String publisher_name = executeSimpleSelectQuery("SELECT ?publisher_name WHERE {<" + datasetURI + "> <" + DCTERMS.PUBLISHER + ">/<" + FOAF.NAME + "> ?publisher_name FILTER(LANGMATCHES(LANG(?publisher_name), \"" + configuration.getLoadLanguage() + "\"))}", "publisher_name");

        if (publisher_uri != null && !publisher_uri.isEmpty() && !organizations.containsKey(publisher_uri)) {
            LOG.debug("Creating organization " + publisher_uri);
            JSONObject root = new JSONObject();

            if (publisher_name == null || publisher_name.isEmpty()) {
                throw exceptionFactory.failure("Organization has no name: " + publisher_uri);
            }

            root.put("title", publisher_name);
            String orgname = Normalizer.normalize(publisher_name, Normalizer.Form.NFD)
                    .replaceAll("\\P{InBasic_Latin}", "")
                    .replace(' ', '-')
                    .replace('.', '-')
                    .toLowerCase();
            root.put("name", orgname);
            JSONArray org_extras = new JSONArray();
            org_extras.put(new JSONObject().put("key", "uri").put("value", publisher_uri));
            root.put("extras", org_extras);

            HttpPost httpPost = new HttpPost(apiURI + "/organization_create");
            httpPost.addHeader(new BasicHeader("Authorization", configuration.getApiKey()));

            String json = root.toString();

            httpPost.setEntity(new StringEntity(json, Charset.forName("utf-8")));

            CloseableHttpResponse response = null;

            try {
                response = postClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 200) {
                    LOG.debug("Organization created OK");
                    //LOG.info("Response: " + EntityUtils.toString(response.getEntity()));
                    organizations.put(publisher_uri, orgname);
                } else if (response.getStatusLine().getStatusCode() == 409) {
                    String ent = EntityUtils.toString(response.getEntity());
                    LOG.error("Organization conflict: " + ent);
                    throw exceptionFactory.failure("Organization conflict: " + ent);
                } else {
                    String ent = EntityUtils.toString(response.getEntity());
                    LOG.error("Response:" + ent);
                    throw exceptionFactory.failure("Error creating organization: " + ent);
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        throw exceptionFactory.failure("Error creating dataset");
                    }
                }
            }
        }

        LOG.debug("Creating JSON");

        JSONObject root = new JSONObject();

        JSONArray tags = new JSONArray();
        for (String keyword : keywords) {
            String safekeyword = fixKeyword(keyword);
            if (safekeyword.length() >= 2) {
                tags.put(new JSONObject().put("name", safekeyword));
            }
        }
        root.put("tags", tags);

        JSONArray resources = new JSONArray();

        if (!datasetID.isEmpty()) {
            root.put("name", datasetID);
        }

        String title = executeSimpleSelectQuery("SELECT ?title WHERE {<" + datasetURI + "> <" + DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"" + configuration.getLoadLanguage() + "\"))}", "title");
        if (!title.isEmpty()) {
            root.put("title", title);
        }
        String description = executeSimpleSelectQuery("SELECT ?description WHERE {<" + datasetURI + "> <" + DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"" + configuration.getLoadLanguage() + "\"))}", "description");
        if (!description.isEmpty()) {
            root.put("notes", description);
        }
        String contactPoint = executeSimpleSelectQuery("SELECT ?contact WHERE {<" + datasetURI + "> <" + DcatAp11ToCkanVocabulary.DCAT_CONTACT_POINT + ">/<" + DcatAp11ToCkanVocabulary.VCARD_HAS_EMAIL + "> ?contact }", "contact");
        if (!contactPoint.isEmpty()) {
            root.put("maintainer_email", contactPoint);
        }
        String curatorName = executeSimpleSelectQuery("SELECT ?name WHERE {<" + datasetURI + "> <" + DcatAp11ToCkanVocabulary.DCAT_CONTACT_POINT + ">/<" + DcatAp11ToCkanVocabulary.VCARD_FN + "> ?name }", "name");
        if (!curatorName.isEmpty()) {
            root.put("maintainer", curatorName);
        }
        String issued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + datasetURI + "> <" + DCTERMS.ISSUED + "> ?issued }", "issued");
        if (!issued.isEmpty()) {
            root.put("metadata_created", issued);
        }
        String modified = executeSimpleSelectQuery("SELECT ?modified WHERE {<" + datasetURI + "> <" + DCTERMS.MODIFIED + "> ?modified }", "modified");
        if (!modified.isEmpty()) {
            root.put("metadata_modified", modified);
        }

        if (configuration.getProfile().equals(DcatAp11ToCkanVocabulary.PROFILES_NKOD.stringValue())) {
            if (!publisher_uri.isEmpty()) {
                root.put("publisher_uri", publisher_uri);
            }
            if (!publisher_name.isEmpty()) {
                root.put("publisher_name", publisher_name);
            }

            String periodicity = executeSimpleSelectQuery("SELECT ?periodicity WHERE {<" + datasetURI + "> <"+ DCTERMS.ACCRUAL_PERIODICITY + "> ?periodicity }", "periodicity");
            if (!periodicity.isEmpty()) {
                root.put("frequency", periodicity);
            }
            String temporalStart = executeSimpleSelectQuery("SELECT ?temporalStart WHERE {<" + datasetURI + "> <"+ DCTERMS.TEMPORAL + ">/<" + DcatAp11ToCkanVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
            if (!temporalStart.isEmpty()) {
                root.put("temporal_start", temporalStart);
            }
            String temporalEnd = executeSimpleSelectQuery("SELECT ?temporalEnd WHERE {<" + datasetURI + "> <"+ DCTERMS.TEMPORAL + ">/<" + DcatAp11ToCkanVocabulary.SCHEMA_ENDDATE  + "> ?temporalEnd }", "temporalEnd");
            if (!temporalEnd.isEmpty()) {
                root.put("temporal_end", temporalEnd);
            }
            String schemaURL = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + datasetURI + "> <"+ FOAF.PAGE + "> ?schema }", "schema");
            if (!schemaURL.isEmpty()) {
                root.put("schema", schemaURL);
            }
            String spatial = executeSimpleSelectQuery("SELECT ?spatial WHERE {<" + datasetURI + "> <"+ DCTERMS.SPATIAL + "> ?spatial }", "spatial");
            if (!spatial.isEmpty()) {
                root.put("spatial_uri", spatial);
            }
            LinkedList<String> themes = new LinkedList<>();
            for (Map<String,Value> map: executeSelectQuery("SELECT ?theme WHERE {<" + datasetURI + "> <"+ DcatAp11ToCkanVocabulary.DCAT_THEME + "> ?theme }")) {
                themes.add(map.get("theme").stringValue());
            }
            String concatThemes = "";
            for (String theme: themes) { concatThemes += theme + " ";}
            if (!concatThemes.isEmpty())  root.put("theme", concatThemes);

        }

        //Distributions

        LinkedList<String> distributions = new LinkedList<>();
        for (Map<String, Value> map : executeSelectQuery("SELECT ?distribution WHERE {<" + datasetURI + "> <" + DcatAp11ToCkanVocabulary.DCAT_DISTRIBUTION + "> ?distribution }")) {
            distributions.add(map.get("distribution").stringValue());
        }

        for (String distribution : distributions) {
            JSONObject distro = new JSONObject();

            String dtitle = executeSimpleSelectQuery("SELECT ?title WHERE {<" + distribution + "> <" + DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"" + configuration.getLoadLanguage() + "\"))}", "title");
            if (!dtitle.isEmpty()) {
                distro.put("name", dtitle);
            }
            String ddescription = executeSimpleSelectQuery("SELECT ?description WHERE {<" + distribution + "> <" + DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"" + configuration.getLoadLanguage() + "\"))}", "description");
            if (!ddescription.isEmpty()) {
                distro.put("description", ddescription);
            }
            //DCAT-AP v1.1: has to be an IRI from http://publications.europa.eu/mdr/authority/file-type/index.html
            String dformat = executeSimpleSelectQuery("SELECT ?format WHERE {<" + distribution + "> <"+ DCTERMS.FORMAT + "> ?format }", "format");
            if (!dformat.isEmpty() && codelists != null) {
                String formatlabel = executeSimpleCodelistSelectQuery("SELECT ?formatlabel WHERE {<" + dformat + "> <"+ SKOS.PREF_LABEL + "> ?formatlabel FILTER(LANGMATCHES(LANG(?formatlabel), \"en\"))}", "formatlabel");
                if (!formatlabel.isEmpty()) {
                    distro.put("format", formatlabel);
                }
            }


            String dwnld = executeSimpleSelectQuery("SELECT ?dwnld WHERE {<" + distribution + "> <" + DcatAp11ToCkanVocabulary.DCAT_DOWNLOADURL + "> ?dwnld }", "dwnld");
            String access = executeSimpleSelectQuery("SELECT ?acc WHERE {<" + distribution + "> <" + DcatAp11ToCkanVocabulary.DCAT_ACCESSURL + "> ?acc }", "acc");

            //we prefer downloadURL, but only accessURL is mandatory
            if (dwnld == null || dwnld.isEmpty()) {
                dwnld = access;
                if (dwnld == null || dwnld.isEmpty()) {
                    LOG.warn("Empty download and access URLs: " + datasetURI);
                    continue;
                }
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

            String dissued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + distribution + "> <" + DCTERMS.ISSUED + "> ?issued }", "issued");
            if (!dissued.isEmpty()) {
                distro.put("created", dissued);
            }
            String dmodified = executeSimpleSelectQuery("SELECT ?modified WHERE {<" + distribution + "> <" + DCTERMS.MODIFIED + "> ?modified }", "modified");
            if (!dmodified.isEmpty()) {
                distro.put("last_modified", dmodified);
            }

            if (configuration.getProfile().equals(DcatAp11ToCkanVocabulary.PROFILES_NKOD.stringValue())) {
                String dtemporalStart = executeSimpleSelectQuery("SELECT ?temporalStart WHERE {<" + distribution + "> <"+ DCTERMS.TEMPORAL + ">/<" + DcatAp11ToCkanVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
                if (!dtemporalStart.isEmpty()) {
                    distro.put("temporal_start", dtemporalStart);
                }
                String dtemporalEnd = executeSimpleSelectQuery("SELECT ?temporalEnd WHERE {<" + distribution + "> <"+ DCTERMS.TEMPORAL + ">/<" + DcatAp11ToCkanVocabulary.SCHEMA_ENDDATE  + "> ?temporalEnd }", "temporalEnd");
                if (!dtemporalEnd.isEmpty()) {
                    distro.put("temporal_end", dtemporalEnd);
                }
                String dspatial = executeSimpleSelectQuery("SELECT ?spatial WHERE {<" + distribution + "> <"+ DCTERMS.SPATIAL + "> ?spatial }", "spatial");
                if (!dspatial.isEmpty()) {
                    root.put("spatial_uri", dspatial);
                }
                String dschemaURL = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + distribution + "> <"+ DCTERMS.CONFORMS_TO + "> ?schema }", "schema");
                if (!dschemaURL.isEmpty()) {
                    distro.put("describedBy", dschemaURL);
                }
                String dlicense = executeSimpleSelectQuery("SELECT ?license WHERE {<" + distribution + "> <"+ DCTERMS.LICENSE + "> ?license }", "license");
                if (!dlicense.isEmpty()) {
                    distro.put("license_link", dlicense);
                }
                String dmimetype = executeSimpleSelectQuery("SELECT ?format WHERE {<" + distribution + "> <"+ DcatAp11ToCkanVocabulary.DCAT_MEDIATYPE + "> ?format }", "format");
                if (!dmimetype.isEmpty()) {
                    distro.put("mimetype", dmimetype.replaceAll(".*\\/([^\\/]+\\/[^\\/]+)","$1"));
                }
            }

            resources.put(distro);
        }

        //Add the remaining distributions that were not updated but existed in the original dataset
        for (Entry<String, JSONObject> resource : resourceList.entrySet()) {
            resources.put(resource.getValue());
        }

        root.put("resources", resources);

        //Create new dataset
        if (!datasetExists) {
            JSONObject createRoot = new JSONObject();
            CloseableHttpResponse response = null;

            createRoot.put("name", datasetID);
            createRoot.put("title", title);
            if (publisher_uri != null && !publisher_uri.isEmpty()) {
                createRoot.put("owner_org", organizations.get(publisher_uri));
            }

            LOG.debug("Creating dataset in CKAN");
            HttpPost httpPost = new HttpPost(apiURI + "/package_create?id=" + datasetID);
            httpPost.addHeader(new BasicHeader("Authorization", configuration.getApiKey()));

            String json = createRoot.toString();

            LOG.debug("Creating dataset with: " + json);

            httpPost.setEntity(new StringEntity(json, Charset.forName("utf-8")));

            try {
                response = createClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 200) {
                    LOG.debug("Dataset created OK");
                    //LOG.info("Response: " + EntityUtils.toString(response.getEntity()));
                } else if (response.getStatusLine().getStatusCode() == 409) {
                    String ent = EntityUtils.toString(response.getEntity());
                    LOG.error("Dataset already exists: " + ent);
                    throw exceptionFactory.failure("Dataset already exists");
                } else {
                    String ent = EntityUtils.toString(response.getEntity());
                    LOG.error("Response:" + ent);
                    throw exceptionFactory.failure("Error creating dataset");
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        throw exceptionFactory.failure("Error creating dataset");
                    }
                }
            }
        }

        //Update existing dataset
        String json = root.toString();
        LOG.debug("Posting to CKAN");
        HttpPost httpPost = new HttpPost(apiURI + "/package_update?id=" + datasetID);
        httpPost.addHeader(new BasicHeader("Authorization", configuration.getApiKey()));

        LOG.debug(json);

        httpPost.setEntity(new StringEntity(json, Charset.forName("utf-8")));
        CloseableHttpResponse response = null;

        try {
            response = postClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                //LOG.info("Response:" + EntityUtils.toString(response.getEntity()));
            } else {
                String ent = EntityUtils.toString(response.getEntity());
                LOG.error("Response:" + ent);
                throw exceptionFactory.failure("Error updating dataset");
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    throw exceptionFactory.failure("Error updating dataset");
                }
            }
        }

        progressReport.entryProcessed();

        try {
            queryClient.close();
            createClient.close();
            postClient.close();
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        progressReport.done();

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
