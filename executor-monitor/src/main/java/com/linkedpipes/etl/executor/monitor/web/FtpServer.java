package com.linkedpipes.etl.executor.monitor.web;

import com.linkedpipes.etl.executor.monitor.Configuration;
import com.linkedpipes.etl.executor.monitor.debug.ftp.VirtualFileSystem;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.UserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Service;

/**
 * FTP server used to access debug data.
 */
@Service
public class FtpServer implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(FtpServer.class);

    @Autowired
    private VirtualFileSystem virtualFileSystem;

    @Autowired
    private AbstractApplicationContext appContext;

    @Autowired
    private Configuration configuration;

    private org.apache.ftpserver.FtpServer server = null;

    protected void start() {
        final DataConnectionConfigurationFactory dataFactory
                = new DataConnectionConfigurationFactory();

        dataFactory.setActiveEnabled(false);
        dataFactory.setPassivePorts(configuration.getFtpDataPort());

        final ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(configuration.getFtpCommandPort());
        listenerFactory.setDataConnectionConfiguration(
                dataFactory.createDataConnectionConfiguration());

        final ConnectionConfigFactory connectionConfigFactory
                = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);

        final BaseUser anonymous = new BaseUser();
        anonymous.setName("anonymous");
        anonymous.setPassword("");
        anonymous.setHomeDirectory("");

        final UserManagerFactory userManagerFactory
                = new PropertiesUserManagerFactory();
        final UserManager userManager = userManagerFactory.createUserManager();
        try {
            userManager.save(anonymous);
        } catch (FtpException ex) {
            LOG.error("Can't add anonymous user.", ex);
            appContext.stop();
            return;
        }

        final FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.addListener("default", listenerFactory.createListener());
        serverFactory.setConnectionConfig(
                connectionConfigFactory.createConnectionConfig());
        serverFactory.setUserManager(userManager);

        serverFactory.setFileSystem((User user) -> {
            return virtualFileSystem.getView();
        });

        this.server = serverFactory.createServer();
        try {
            this.server.start();
        } catch (FtpException ex) {
            LOG.error("Can't start FTP server.", ex);
            appContext.stop();
        }
    }

    protected void stop() {
        if (server != null) {
            server.stop();
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
