package com.linkedpipes.commons.code.web.boundary;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 *
 * @author Škoda Petr
 */
public final class JettyServerBuilder {

    private static class JettyServer implements WebServerContainer {

        final Server server;

        public JettyServer(Server server) {
            this.server = server;
        }

        @Override
        public boolean start() {
            try {
                server.start();
                return true;
            } catch (Exception ex) {
                LOG.error("Can't start web server.", ex);
                return false;
            }
        }

        @Override
        public void stop() {
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

    }

    private static final Logger LOG = LoggerFactory.getLogger(JettyServerBuilder.class);

    private final AbstractApplicationContext parentContext;

    private final int port;

    private final ServletContextHandler ctxHandler;

    private QueuedThreadPool threadPool = null;

    private HandlerWrapper ctxHandlerWrap = null;

    public JettyServerBuilder(AbstractApplicationContext parentContext, int port) {
        this.parentContext = parentContext;
        this.port = port;
        // ...
        ctxHandler = new ServletContextHandler();
        ctxHandler.setErrorHandler(null);
        ctxHandler.setContextPath("/"); // <url-pattern>/</url-pattern>
    }

    public void addServlet(String springConfigurationFile, String webPathPattern, String resourcePath)
            throws IOException {
        final XmlWebApplicationContext webContext = new XmlWebApplicationContext();
        webContext.setParent(parentContext);
        webContext.setConfigLocation(springConfigurationFile);

        final DispatcherServlet dispatcher = new DispatcherServlet(webContext);
        final ServletHolder servlet = new ServletHolder(dispatcher);

        // Add to context handler.
        ctxHandler.addEventListener(new ContextLoaderListener(webContext));
        ctxHandler.addServlet(servlet, webPathPattern);
        ctxHandler.setResourceBase(new ClassPathResource(resourcePath).getURI().toString());
    }

    public void setMdcWrap(String mdcValue) {
        ctxHandlerWrap = new HandlerWrapper() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {
                MDC.put(mdcValue, null);
                try {
                    super.handle(target, baseRequest, request, response);
                } finally {
                    MDC.remove(mdcValue);
                }
            }

        };
        ctxHandlerWrap.setHandler(ctxHandler);
    }

    public void customizeThreads(String threadsName, int minThreads, int maxThreads) {
        threadPool = new QueuedThreadPool() {

            @Override
            protected Thread newThread(Runnable runnable) {
                final Thread newThread = super.newThread(runnable);
                newThread.setName(threadsName);
                return newThread;
            }

        };
        threadPool.setMinThreads(minThreads);
        threadPool.setMaxThreads(maxThreads);
    }


    public WebServerContainer create() {
        final Server server = new Server(threadPool);

        // Prepare connector.
        final ServerConnector http = new ServerConnector(server);
        http.setPort(port);

        server.addConnector(http);
        if (ctxHandlerWrap == null) {
            server.setHandler(ctxHandler);
        } else {
            server.setHandler(ctxHandlerWrap);
        }
        server.setStopAtShutdown(true);

        return new JettyServer(server);
    }

}
