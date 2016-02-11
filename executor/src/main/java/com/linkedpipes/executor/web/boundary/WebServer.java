package com.linkedpipes.executor.web.boundary;

import com.linkedpipes.commons.code.web.boundary.AbstractWebServer;
import com.linkedpipes.commons.code.web.boundary.JettyServerBuilder;
import com.linkedpipes.commons.code.web.boundary.WebServerContainer;
import org.springframework.beans.factory.annotation.Autowired;

import com.linkedpipes.executor.Configuration;

/**
 *
 * @author Škoda Petr
 */
public class WebServer extends AbstractWebServer {

    @Autowired
    private Configuration configuration;

    @Override
    protected WebServerContainer getWebServer() throws Exception {
        final JettyServerBuilder builder = new JettyServerBuilder(appContext,
                configuration.getWebServerPort());
        builder.addServlet("spring/context-web.xml", "/api/v1/*", "/web/");
        builder.customizeThreads("web-server", 1, 8);
        return builder.create();
    }

}
