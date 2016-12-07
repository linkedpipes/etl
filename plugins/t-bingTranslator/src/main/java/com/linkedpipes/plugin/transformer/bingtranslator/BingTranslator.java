package com.linkedpipes.plugin.transformer.bingtranslator;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.ChunkedStatements;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableChunkedStatements;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public final class BingTranslator implements Component.Sequential {

    /**
     * The maximum number of entries to translate for one request.
     */
    private static final int BING_QUERY_LIMIT = 300;

    /**
     * Refresh time for API token, the time to live time is 10 minutes,
     * 8 minutes are recommended for refresh rate.
     */
    private static final int API_TOKEN_TTL = 1000 * 60 * 8;

    public static final String ISSUE_TOKEN_URL =
            "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";

    public static final String TRANSLATE_URL =
            "https://api.microsofttranslator.com/v2/http.svc/TranslateArray";

    private static final String TYPE_STRING =
            "http://www.w3.org/2001/XMLSchema#string";

    private static final String TYPE_LANG_STRING =
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";

    private static final Logger LOG =
            LoggerFactory.getLogger(BingTranslator.class);

    @Component.InputPort(id = "FilesInput")
    public ChunkedStatements input;

    @Component.OutputPort(id = "FilesOutput")
    public WritableChunkedStatements output;

    @Component.Configuration
    public BingTranslatorConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    /**
     * Current token in used. The bing require us to update this
     * every 8 minute (10 minute is expiration time).
     */
    private String token = null;

    /**
     * Time of retrieval of current API key.
     */
    private long tokenRetrieval = 0;

    /**
     * Parser for XML.
     */
    private SAXParser saxParser;

    /**
     * Instance of OK response SAX handler.
     */
    private final SaxResponseHandler responseHandler = new SaxResponseHandler();

    /**
     * Statement collector, used to buffer statements before they
     * are stored as a chunk.
     */
    private List<Statement> outputBuffer = new ArrayList<>(10000);

    @Override
    public void execute() throws LpException {
        // Initialize sax parser.
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            this.saxParser = factory.newSAXParser();
        } catch (ParserConfigurationException | SAXException ex) {
            throw exceptionFactory.failure("Can't create SAX parser,", ex);
        }
        // We use LinkedHashMap to preserve ordering.
        final Map<String, LinkedHashMap<String, List<Statement>>> data =
                new HashMap<>();
        // Read chunks.
        progressReport.start(input.size());
        for (ChunkedStatements.Chunk chunk : input) {
            // Load values from chunk.
            for (Statement s : chunk.toStatements()) {
                if (!(s.getObject() instanceof Literal)) {
                    continue;
                }
                Literal literal = (Literal) s.getObject();
                final String dataType = literal.getDatatype().stringValue();
                final String language;
                final String value = literal.getLabel();
                if (TYPE_STRING.equals(dataType)) {
                    language = configuration.getDefaultLanguage();
                } else if (TYPE_LANG_STRING.equals(dataType)) {
                    language = literal.getLanguage().get();
                } else {
                    // Some other type.
                    continue;
                }
                // Create record.
                if (!data.containsKey(language)) {
                    data.put(language, new LinkedHashMap<>());
                }
                if (!data.get(language).containsKey(value)) {
                    data.get(language).put(value, new ArrayList<>(16));
                }
                data.get(language).get(value).add(s);
            }
            // Translate chunk content.
            for (Map.Entry<String, LinkedHashMap<String, List<Statement>>>
                    entry : data.entrySet()) {
                for (String lang : configuration.getTargetLanguages()) {
                    if (lang.equals(entry.getKey())) {
                        // Labels are already in required language.
                        continue;
                    }
                    translate(entry.getValue(), entry.getKey(), lang);
                }
            }
            // Save.
            output.submit(outputBuffer);
            outputBuffer.clear();
            //
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void translate(LinkedHashMap<String, List<Statement>> data,
            String fromLanguage, String toLanguage) throws LpException {
        // Check entity count.
        if (data.size() > BING_QUERY_LIMIT) {
            splitAndTransform(data, fromLanguage, toLanguage);
            return;
        }
        // Check request size -> for this we need a valid token.
        checkApiKey();
        final String requestBody = prepareRequest(data.keySet(), token,
                fromLanguage, toLanguage);
        if (requestBody == null) {
            splitAndTransform(data, fromLanguage, toLanguage);
            return;
        }
        //
        LOG.info("Translate: {} {} -> {} ...", data.size(), fromLanguage,
                toLanguage);
        // Prepare request.
        final HttpPost post = new HttpPost(TRANSLATE_URL);
        post.addHeader("Content-Type", "application/xml");
        post.addHeader("Accept", "application/xml");
        final HttpEntity entity = new StringEntity(requestBody,
                ContentType.create("text/plain", Consts.UTF_8));
        post.setEntity(entity);
        // Execute request.
        final CloseableHttpClient httpClient = HttpClients.custom().build();
        final HttpClientContext context = HttpClientContext.create();
        try (final CloseableHttpResponse response
                     = httpClient.execute(post, context)) {
            final String responseString;
            try {
                responseString = EntityUtils.toString(response.getEntity());
            } catch (java.net.SocketException ex) {
                throw exceptionFactory.failure("Can't read response", ex);
            }
            //
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 200) {
                try {
                    storeResults(data, parseOkResponse(responseString),
                            fromLanguage, toLanguage);
                } catch (LpException ex) {
                    LOG.info("Request:\n{}\nResponse:\n{}",
                            requestBody, responseString);
                    throw ex;
                }
                return;
            }
            // We need to detect the error.
            if (responseString.contains("The incoming token has expired")) {
                throw exceptionFactory.failure("The token expired before " +
                                "use, retrieval: {}, current: {}) : {}\n{}",
                        tokenRetrieval, (new Date()).getTime(),
                        responseCode, responseString);
            }
            // Check for response.
            parserErrorResponse(data, fromLanguage, toLanguage, responseCode,
                    response.getStatusLine().getReasonPhrase(), responseString);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't execute request.", ex);
        }
    }

    private void parserErrorResponse(
            LinkedHashMap<String, List<Statement>> data,
            String fromLanguage, String toLanguage,
            int responseCode, String responsePhrase,
            String responseString) throws LpException {
        // Check for known error responses.
        if (responseCode == 413
                || responseString.contains(BingErrorResponse.TOO_MANY_ELEMENTS)
                || responseString.contains(BingErrorResponse.TOO_MUCH_DATA)) {
            // We just try to split the data in half.
            if (data.size() <= 1) {
                throw exceptionFactory.failure(
                        "Can't further divide the data. Response {} : {}\n{}",
                        responseCode, responsePhrase, responseString);
            }
            splitAndTransform(data, fromLanguage, toLanguage);
            return;
        }

        // Other Error codes:
        // See http://docs.microsofttranslator.com/text-translate.html#
        // for more information.
        //Array element cannot be empty
        //Invalid category
        //from is invalid
        //To is invalid
        //The from language is not supported
        //The to language is not supported
        //Invalid operation
        //Html is not in a correct format
        //Too many strings were passed in the Translate Request
        //Invalid credentials
        //Service temporarily unavailable

        //
        throw exceptionFactory.failure("{} : {}\n{}", responseCode,
                responsePhrase, responseString);
    }

    /**
     * Split data into two and transform them.
     *
     * @param data
     * @param fromLanguage
     * @param toLanguage
     */
    private void splitAndTransform(
            LinkedHashMap<String, List<Statement>> data,
            String fromLanguage, String toLanguage) throws LpException {
        // Check size.
        if (data.size() <= 1) {
            LOG.info("Data:");
            for (String s : data.keySet()) {
                LOG.info("\t{}", s);
            }
            throw exceptionFactory.failure("Can't further split data.");
        }

        //
        LOG.info("Splitting ({}) ...", data.size());
        final LinkedHashMap<String, List<Statement>> first
                = new LinkedHashMap<>();
        final LinkedHashMap<String, List<Statement>> second
                = new LinkedHashMap<>();
        //
        int divider = data.size() / 2;
        final Iterator<Map.Entry<String, List<Statement>>> iterator
                = data.entrySet().iterator();
        for (int index = 0; index < divider; ++index) {
            final Map.Entry<String, List<Statement>> item = iterator.next();
            first.put(item.getKey(), item.getValue());
        }
        for (int index = divider; index < data.size(); ++index) {
            final Map.Entry<String, List<Statement>> item = iterator.next();
            second.put(item.getKey(), item.getValue());
        }
        //
        translate(first, fromLanguage, toLanguage);
        translate(second, fromLanguage, toLanguage);
    }

    /**
     * Require {@link #saxParser} to be initialized.
     *
     * @param response
     * @return
     */
    private List<String> parseOkResponse(String response) throws LpException {
        responseHandler.getValues().clear();
        try {
            saxParser.parse(new ByteArrayInputStream(
                    response.getBytes("utf-8")), responseHandler);
        } catch (UnsupportedEncodingException ex) {
            throw exceptionFactory.failure("UTF-8 is not supported.", ex);
        } catch (IOException | SAXException ex) {
            throw exceptionFactory.failure("Can't parser response.", ex);
        }
        return responseHandler.getValues();
    }

    /**
     * The ordering of source and target must be the same.
     *
     * @param data
     * @param target
     * @param fromLanguage
     * @param toLanguage
     */
    private void storeResults(LinkedHashMap<String, List<Statement>> data,
            List<String> target, String fromLanguage, String toLanguage)
            throws LpException {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        if (data.size() != target.size()) {
            LOG.info("Source:");
            for (String s : data.keySet()) {
                LOG.info("{}\t", s);
            }
            LOG.info("Target:");
            for (String s : target) {
                LOG.info("{}\t", s);
            }
            throw exceptionFactory.failure("Number of entities in request " +
                            "and response is not equal ({}:{})",
                    data.size(), target.size());
        }
        final String languageTag = createTranslatedLanguageTag(fromLanguage,
                toLanguage);
        Iterator<String> iter = data.keySet().iterator();
        for (int i = 0; i < data.size(); ++i) {
            final String literal = target.get(i);
            final List<Statement> statements = data.get(iter.next());
            //
            for (Statement s : statements) {
                outputBuffer.add(valueFactory.createStatement(
                        s.getSubject(), s.getPredicate(),
                        valueFactory.createLiteral(literal, languageTag)
                ));
            }
        }
    }

    /**
     * Create language tag for given translation.
     *
     * @param fromLanguage
     * @param toLanguage
     * @return
     */
    private String createTranslatedLanguageTag(String fromLanguage,
            String toLanguage) {
        return toLanguage + "-t-" + fromLanguage;
    }

    /**
     * Prepare and return string form of XML request to Bing translation
     * service.
     *
     * @param literals
     * @param token
     * @param fromLanguage
     * @param toLanguage
     * @return Null if the request is too big.
     */
    private static String prepareRequest(Iterable<String> literals,
            String token, String fromLanguage, String toLanguage) {
        // Parse language tag.
        if (fromLanguage.contains("-t-")) {
            fromLanguage = fromLanguage.substring(0,
                    fromLanguage.indexOf("-t-"));
        }
        //
        final StringBuilder request = new StringBuilder();
        request.append("<TranslateArrayRequest>");
        request.append("<AppId>Bearer ");
        request.append(token);
        request.append("</AppId>");
        request.append("<From>");
        request.append(fromLanguage);
        request.append("</From>");
        request.append("<Options>");
        request.append("<ContentType>text/plain</ContentType>");
        request.append("</Options>");
        request.append("<Texts>");
        // From BING:
        // Message: the parameter 'texts' must be less than '10241' characters
        final StringBuilder texts = new StringBuilder();
        for (String string : literals) {
            texts.append("<string xmlns=\"http://schemas.microsoft.com/" +
                    "2003/10/Serialization/Arrays\">");
            texts.append(StringEscapeUtils.escapeXml11(string));
            texts.append("</string>");
        }
        if (texts.length() > 10240) {
            LOG.info("Texts size too big ({})", texts.length());
            return null;
        }
        request.append(texts.toString());
        request.append("</Texts>");
        request.append("<To>");
        request.append(toLanguage);
        request.append("</To>");
        request.append("</TranslateArrayRequest>");
        return request.toString();
    }

    /**
     * Check API key, if it's not valid, update it.
     */
    private void checkApiKey() throws LpException {
        if (tokenRetrieval + API_TOKEN_TTL > (new Date()).getTime()) {
            // The key is valid.
            return;
        }
        long retrieveStartTime = (new Date()).getTime();
        final HttpPost post = new HttpPost(ISSUE_TOKEN_URL);
        post.addHeader("Ocp-Apim-Subscription-Key",
                configuration.getSubscriptionKey());
        final CloseableHttpClient httpClient = HttpClients.custom().build();
        final HttpClientContext context = HttpClientContext.create();
        try (final CloseableHttpResponse response
                     = httpClient.execute(post, context)) {
            final String responseString;
            try {
                responseString = EntityUtils.toString(response.getEntity());
            } catch (java.net.SocketException ex) {
                throw exceptionFactory.failure("Can't read response", ex);
            }
            switch (response.getStatusLine().getStatusCode()) {
                case 401:
                    throw exceptionFactory.failure("Unauthorized. " +
                            "Ensure that the key provided is valid.");
                case 403:
                    throw exceptionFactory.failure("Unauthorized. " +
                            "For an account in the free-tier, this indicates " +
                            "that the account quota has been exceeded.");
                case 200:
                    break;
                default:
                    throw exceptionFactory.failure(
                            "Request for token failed: {} \n {}",
                            response.getStatusLine().getStatusCode(),
                            responseString);
            }
            //
            token = responseString;
            tokenRetrieval = retrieveStartTime;
        } catch (IOException ex) {
            throw exceptionFactory.failure("Request for token failed.", ex);
        }
    }

}
