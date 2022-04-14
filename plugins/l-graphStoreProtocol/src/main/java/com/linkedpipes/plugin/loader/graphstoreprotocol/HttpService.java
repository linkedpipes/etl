package com.linkedpipes.plugin.loader.graphstoreprotocol;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;

public class HttpService {

    private static final Logger LOG =
            LoggerFactory.getLogger(HttpService.class);

    private final boolean useAuthentication;

    private final String userName;

    private final String password;

    private final String authenticationEndpoint;

    public HttpService(
            boolean useAuthentication,
            String userName, String password,
            String authenticationEndpoint) {
        this.useAuthentication = useAuthentication;
        this.userName = userName;
        this.password = password;
        this.authenticationEndpoint = authenticationEndpoint;
    }

    public void executeHttp(HttpEntityEnclosingRequestBase httpMethod)
            throws LpException {
        CloseableHttpClient httpClient = createHttpClient();
        HttpClientContext context = HttpClientContext.create();
        if (useAuthentication) {
            authenticate(httpClient, context);
        }
        //
        try (CloseableHttpResponse response
                     = httpClient.execute(httpMethod, context)) {
            logResponseEntity(response);
            checkResponseCode(response);
        } catch (IOException | ParseException ex) {
            try {
                httpClient.close();
            } catch (IOException closingEx) {
                // We only log here as there is already exception thrown.
                LOG.error("Can't close request.", closingEx);
            }
            throw new LpException("Can't execute request.", ex);
        }
        try {
            httpClient.close();
        } catch (IOException ex) {
            throw new LpException("Can't close request.", ex);
        }
    }

    protected CloseableHttpClient createHttpClient() {
        if (useAuthentication) {
            // Use preemptive authentication.
            CredentialsProvider creds = new BasicCredentialsProvider();
            creds.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(userName, password));
            //
            RequestConfig requestConfig = RequestConfig.custom()
                    .setAuthenticationEnabled(true).build();
            //
            return HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultCredentialsProvider(creds)
                    .build();
        } else {
            return HttpClients.custom().build();
        }
    }

    /**
     * Do an empty request just to get the validation into a cache.
     * This is requires as for example Virtuoso will refuse the first
     * request and ask for authorization. However, as the first request
     * can be too big - it would look like a failure to us
     * (as Virtuoso just close the connection before reading all the data).
     */
    protected void authenticate(
            CloseableHttpClient httpClient,
            HttpClientContext context) {
        var request = new HttpPut(authenticationEndpoint);
        try {
            CloseableHttpResponse response =
                    httpClient.execute(request, context);
            response.close();
        } catch (Exception ex) {
            LOG.info("Exception during first empty request:", ex);
        }
    }

    protected void logResponseEntity(CloseableHttpResponse response)
            throws IOException {
        LOG.info("Response code: {} phrase: {}",
                response.getStatusLine().getStatusCode(),
                response.getStatusLine().getReasonPhrase());
        try {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return;
            }
            LOG.debug("Response:\n {} ",EntityUtils.toString(entity));
        } catch (SocketException ex) {
            LOG.error("Can't read response entity.", ex);
        }
    }

    protected void checkResponseCode(CloseableHttpResponse response)
            throws LpException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 400) {
            throw new LpException(
                    "Can't upload data, status: {} \n Server response: {}",
                    statusCode, response.getStatusLine().getReasonPhrase());
        }
    }

}
