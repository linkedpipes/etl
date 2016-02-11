package com.linkedpipes.commons.code.web.boundary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.support.AbstractApplicationContext;

/**
 *
 * @author Petr Škoda
 */
public abstract class AbstractWebServer implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractWebServer.class);

    @Autowired
    protected AbstractApplicationContext appContext;

    private WebServerContainer webServer = null;

    protected abstract WebServerContainer getWebServer() throws Exception;

    protected void start() {
        try {
            webServer = getWebServer();
        } catch (Exception ex) {
            LOG.error("Can't set resource base.", ex);
            appContext.stop();
        }
        if (!webServer.start()) {
            appContext.stop();
        }
    }

    protected void stop() {
        if (webServer != null) {
            webServer.stop();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextStartedEvent) {
            start();
        } else if (event instanceof ContextStoppedEvent) {
            stop();
        }
    }

}
