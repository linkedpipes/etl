package com.linkedpipes.etl.executor.monitor.web;

import com.linkedpipes.etl.executor.monitor.ConfigurationHolder;
import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.nio.file.Files;
import java.nio.file.Path;

@Service
class WebServer implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);

    private static final int MAX_THREADS = 4;

    private final ConfigurationHolder configuration;

    private final AbstractApplicationContext appContext;

    private Server server = null;

    @Autowired
    public WebServer(
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
        LOG.info("Starting web server on port: {}",
                configuration.getWebServerPort());
        //
        ServletContextHandler handler = new ServletContextHandler();
        handler.setErrorHandler(null);
        handler.setContextPath("/");
        handler.setResourceBase(new ClassPathResource("/web/").getURI().toString());
        // Context
        XmlWebApplicationContext webContext = new XmlWebApplicationContext();
        webContext.setParent(appContext);
        webContext.setConfigLocation("spring/context-web.xml");
        handler.addEventListener(new ContextLoaderListener(webContext));
        // Servlet
        DispatcherServlet dispatcher = new DispatcherServlet(webContext);
        ServletHolder servlet = new ServletHolder(dispatcher);
        handler.addServlet(servlet, "/api/v1/*");
        //
        server = new Server(createThreadPool());
        // Connector.
        ServerConnector http = new ServerConnector(server, 1, 1);
        http.setPort(configuration.getWebServerPort());
        // Multipart configuration.
        Path uploadDirectory = Files.createTempDirectory("lp-storage-upload");
        long maxFileSize = 16 * 1024 * 1024;
        long maxRequestSize = 32 * 1024 * 1024;
        int writeToDiskFileSizeThreshold = 2 * 1024 * 1024;
        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                uploadDirectory.toString(),
                maxFileSize,
                maxRequestSize,
                writeToDiskFileSizeThreshold);
        servlet.getRegistration().setMultipartConfig(multipartConfig);
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
