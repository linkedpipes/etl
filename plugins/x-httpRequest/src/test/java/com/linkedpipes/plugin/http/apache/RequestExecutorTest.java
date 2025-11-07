package com.linkedpipes.plugin.http.apache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class RequestExecutorTest {

    /**
     * https://github.com/linkedpipes/etl/issues/998
     */
    @Test
    @Tag("issues")
    @Tag("external-dependency")
    public void optionsWithRedirect() throws Exception {
        var request = new HttpRequest();
        request.url = "https://www.umpod.cz/web/cz/opendata-uredni-deska";
        request.method = "OPTIONS";
        request.followRedirect = true;
        request.headers.put("Origin", "https://data.gov.cz");
        request.headers.put("Access-Control-Request-Method", "GEt");
        var executor = new RequestExecutor(request, response -> {
            var statusLine = response.getStatusLine();
            Assertions.assertEquals(200, statusLine.getStatusCode(),
                    statusLine::getReasonPhrase);
        });
        executor.execute();
    }

    /**
     * https://github.com/linkedpipes/etl/issues/916
     */
    @Test
    @Tag("issues")
    @Tag("external-dependency")
    public void getWithIDN() throws Exception {
        var request = new HttpRequest();
        request.url = "https://obchodní-rejstřík.stirdata.opendata.cz/soubor/or.hdt";
        request.method = "GET";
        var executor = new RequestExecutor(request, response -> {
            var statusLine = response.getStatusLine();
            Assertions.assertEquals(200, statusLine.getStatusCode(),
                    statusLine::getReasonPhrase);
        });
        executor.execute();
    }

    /**
     * https://github.com/linkedpipes/etl/issues/911
     */
    @Test
    @Tag("issues")
    @Tag("external-dependency")
    public void headWithRedirect() throws Exception {
        var request = new HttpRequest();
        request.url = "https://kiosek.justice.cz/opendata/úřední_deska/307000.jsonld";
        request.method = "HEAD";
        var executor = new RequestExecutor(request, response -> {
            var statusLine = response.getStatusLine();
            Assertions.assertEquals(200, statusLine.getStatusCode(),
                    statusLine::getReasonPhrase);
        });
        executor.execute();
    }

}
