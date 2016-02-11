package com.linkedpipes.executor.monitor.web.boundary;

import com.linkedpipes.commons.code.web.boundary.AbstractWebServer;
import org.springframework.beans.factory.annotation.Autowired;

import com.linkedpipes.commons.code.web.boundary.JettyServerBuilder;
import com.linkedpipes.commons.code.web.boundary.WebServerContainer;
import com.linkedpipes.executor.monitor.Configuration;

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
