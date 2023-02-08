package com.linkedpipes.etl.storage.http;

import com.linkedpipes.etl.storage.ConfigurationHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;

@Service
public class HttpServer implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);

    private static final int MAX_THREADS = 4;

    private final ConfigurationHolder configuration;

    private final AbstractApplicationContext appContext;

    private Server server = null;

    public HttpServer(
            ConfigurationHolder configuration,
            AbstractApplicationContext appContext) {
        this.configuration = configuration;
        this.appContext = appContext;
    }

    private void start() {
        try {
            if (server == null) {
                buildServer();
            }
            server.start();
        } catch (Exception ex) {
            LOG.error("Can't start web server. Stopping application.", ex);
            appContext.stop();
        }
    }

    private void stop() {
        LOG.info("Closing web server ...");
        if (server != null) {
            try {
                server.stop();
                server.join();
            } catch (Exception ex) {
                LOG.warn("Can't stop the web server.", ex);
            }
        }
        LOG.info("Closing web server ... done");
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextStartedEvent) {
            start();
        } else if (event instanceof ContextStoppedEvent) {
            stop();
        }
    }

    private void buildServer() throws IOException {
        LOG.info("Starting HTTP server on port {}",
                configuration.getWebServerPort());
        //
        ServletContextHandler handler;
        handler = new ServletContextHandler();
        handler.setErrorHandler(null);
        handler.setContextPath("/");
        // Servlet.
        XmlWebApplicationContext webContext = new XmlWebApplicationContext();
        webContext.setParent(appContext);
        webContext.setConfigLocation("spring/context-web.xml");
        DispatcherServlet dispatcher = new DispatcherServlet(webContext);
        ServletHolder servlet = new ServletHolder(dispatcher);
        handler.addEventListener(new ContextLoaderListener(webContext));
        handler.addServlet(servlet, "/api/v1/*");
        handler.setResourceBase(
                new ClassPathResource("/web/").getURI().toString());
        //
        server = new Server(createThreadPool());
        // Connector.
        ServerConnector http = new ServerConnector(server, 1, 1);
        http.setPort(configuration.getWebServerPort());
        //
        server.addConnector(http);
        server.setHandler(handler);
        server.setStopAtShutdown(true);
    }

    private static QueuedThreadPool createThreadPool() {
        QueuedThreadPool threadPool = new QueuedThreadPool(MAX_THREADS, 1);
        threadPool.setName("web-server");
        return threadPool;
    }

}
