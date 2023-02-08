package com.linkedpipes.etl.executor.monitor;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.linkedpipes.etl.executor.monitor.cli.Configuration;
import com.linkedpipes.etl.executor.monitor.cli.ConfigurationLoader;
import com.linkedpipes.etl.executor.monitor.logging.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

public class ExecutorMonitor {

    private static final Logger LOG =
            LoggerFactory.getLogger(ExecutorMonitor.class);

    private Configuration configuration = null;

    public static void main(String[] args) {
        (new ExecutorMonitor()).run(args);
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
        } catch (MonitorException ex) {
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
        if (configuration.httpPort == null) {
            LOG.error("Missing HTTP port.");
            isConfigurationValid = false;
        }
        if (configuration.executorUrl == null) {
            LOG.error("Missing executor URL.");
            isConfigurationValid = false;
        }
        if (configuration.baseUrl == null) {
            LOG.error("Missing base URL for creating resources.");
            isConfigurationValid = false;
        }
        return isConfigurationValid;
    }

    private void initializeLogging() {
        if (configuration.logDirectory == null) {
            return;
        }
        LoggerContext loggerContext =
                (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logbackLogger =
                loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        String logLevel = configuration.logLevel == null ?
                "INFO" : configuration.logLevel;
        File logDirectory = new File(configuration.logDirectory);
        //
        Appender<ILoggingEvent> appender =
                LoggerUtils.createRollingFileAppender(
                        logDirectory, "executor-monitor",
                        loggerContext, logLevel);
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
