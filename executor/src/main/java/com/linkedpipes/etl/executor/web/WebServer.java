package com.linkedpipes.etl.executor.web;

import com.linkedpipes.etl.executor.Configuration;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
class WebServer implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);

    private Configuration configuration;

    private AbstractApplicationContext appContext;

    private Server server = null;

    @Autowired
    public WebServer(
            Configuration configuration,
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
        LOG.info("Starting server on port: {}",
                configuration.getWebServerPort());
        //
        final ServletContextHandler handler;
        handler = new ServletContextHandler();
        handler.setErrorHandler(null);
        handler.setContextPath("/");
        // Servlet.
        final XmlWebApplicationContext webContext
                = new XmlWebApplicationContext();
        webContext.setParent(appContext);
        webContext.setConfigLocation("spring/context-web.xml");
        final DispatcherServlet dispatcher = new DispatcherServlet(webContext);
        final ServletHolder servlet = new ServletHolder(dispatcher);
        handler.addEventListener(new ContextLoaderListener(webContext));
        handler.addServlet(servlet, "/api/*");
        handler.setResourceBase(
                new ClassPathResource("/web/").getURI().toString());

        //
        server = new Server(createThreadPool());
        // Connector.
        final ServerConnector http = new ServerConnector(server, 1, 1);
        http.setPort(configuration.getWebServerPort());
        //
        server.addConnector(http);
        server.setHandler(wrapHandlerWithMdcContext(handler));
        server.setStopAtShutdown(true);
    }

    private static QueuedThreadPool createThreadPool() {
        final QueuedThreadPool threadPool = new QueuedThreadPool(4, 1);
        threadPool.setName("web-server");
        return threadPool;
    }

    private static HandlerWrapper wrapHandlerWithMdcContext(
            HandlerWrapper handler) {
        final HandlerWrapper handlerWrap = new HandlerWrapper() {

            @Override
            public void handle(
                    String target, Request baseRequest,
                    HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                MDC.put(LoggerFacade.WEB_MDC, null);
                try {
                    super.handle(target, baseRequest, request, response);
                } finally {
                    MDC.remove(LoggerFacade.WEB_MDC);
                }
            }

        };
        handlerWrap.setHandler(handler);
        return handlerWrap;
    }

}
