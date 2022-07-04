package com.linkedpipes.etl.executor;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.linkedpipes.etl.executor.cli.Configuration;
import com.linkedpipes.etl.executor.cli.ConfigurationLoader;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

public class Executor {

    private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

    private Configuration configuration = null;

    public static void main(String[] args) {
        (new Executor()).run(args);
    }

    private void run(String[] args) {
        loadConfiguration(args);
        if (!validateConfiguration()) {
            LOG.info("Invalid configuration.");
            return;
        }
        initializeLogging();
        startSpring();
    }

    private void loadConfiguration(String[] args) {
        ConfigurationLoader loader = new ConfigurationLoader();
        try {
            loader.load(args);
        } catch (ExecutorException ex) {
            LOG.error("Can't load configuration.", ex);
        }
        configuration = loader.getConfiguration();
        ConfigurationHolder.setConfiguration(configuration);
    }

    private boolean validateConfiguration() {
        if (configuration == null) {
            LOG.error("Missing configuration.");
            return false;
        }
        boolean isConfigurationValid = true;
        if (configuration.httpPort() == null) {
            LOG.error("Missing HTTP port.");
            isConfigurationValid = false;
        }
        if (configuration.osgiLibrariesDirectory() == null) {
            LOG.error("Missing OSGI libraries directory.");
            isConfigurationValid = false;
        }
        if (configuration.osgiWorkingDirectory() == null) {
            LOG.error("Missing OSGI working directory.");
            isConfigurationValid = false;
        }
        if (configuration.pluginsDirectory() == null) {
            LOG.error("Missing plugin directory.");
            isConfigurationValid = false;
        }
        return isConfigurationValid;
    }

    private void initializeLogging() {
        if (configuration.logDirectory() == null) {
            return;
        }
        LoggerContext loggerContext =
                (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logbackLogger =
                loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        String logLevel = configuration.logLevel() == null ?
                "INFO" : configuration.logLevel();
        File logDirectory = new File(configuration.logDirectory());
        //
        Appender<ILoggingEvent> appender =
                LoggerFacade.createRollingFileAppender(
                        logDirectory, "executor", loggerContext, logLevel);
        logbackLogger.addAppender(appender);
    }

    private void startSpring() {
        ConfigurableApplicationContext context
                = new ClassPathXmlApplicationContext(
                "spring/context-service.xml");
        context.registerShutdownHook();
        context.start();
    }

}
