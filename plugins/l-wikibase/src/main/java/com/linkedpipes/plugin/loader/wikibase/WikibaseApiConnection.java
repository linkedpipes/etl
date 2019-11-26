package com.linkedpipes.plugin.loader.wikibase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

/**
 * Implement custom exception handling.
 */
class WikibaseApiConnection extends ApiConnection {

    private static final Logger LOG =
            LoggerFactory.getLogger(WikibaseApiConnection.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public WikibaseApiConnection(String apiBaseUrl) {
        super(apiBaseUrl);
    }

    @Override
    public void checkErrors(JsonNode root) throws MediaWikiApiErrorException {
        if (!root.has("error")) {
            return;
        }
        JsonNode errorNode = root.path("error");
        try {
            LOG.error("Response with error node: {}",
                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(errorNode));
        } catch (Exception ex) {
            LOG.info("Can't write node response as JSON string '{}'",
                    errorNode, ex);
        }
        super.checkErrors(root);
    }

}
