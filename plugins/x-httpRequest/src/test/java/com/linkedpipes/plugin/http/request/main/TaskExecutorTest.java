package com.linkedpipes.plugin.http.request.main;

import com.linkedpipes.plugin.http.apache.RequestConfiguration;
import com.linkedpipes.plugin.http.apache.RequestExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TaskExecutorTest {

    /**
     * https://github.com/linkedpipes/etl/issues/916
     */
    @Test
    @Tag("issues")
    @Tag("external-dependency")
    public void getWithIDN() throws Exception {
        var request = new RequestConfiguration();
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
        var request = new RequestConfiguration();
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
