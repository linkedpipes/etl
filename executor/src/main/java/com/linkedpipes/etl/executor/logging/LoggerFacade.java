package com.linkedpipes.etl.executor.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Can be used to manage logging.
 */
public class LoggerFacade {

    public static final String SYSTEM_MDC = "system";

    public static final String COMPONENT_MDC = "component";

    public static final String WEB_MDC = "web";

    private Appender systemAppender = null;

    /**
     * Set new system appender. Destroy any old appender.
     *
     * @param targetfile
     */
    public void setSystemAppender(File targetfile) {
        if (systemAppender != null) {
            destroyAppenders(systemAppender);
        }
        systemAppender = createExecutionAppander(targetfile);
    }

    private static FileAppender createExecutionAppander(File targetFile) {
        final LoggerContext loggerContext
                = (LoggerContext) LoggerFactory.getILoggerFactory();

        final FileAppender appender = new FileAppender();
        appender.setContext(loggerContext);
        appender.setFile(targetFile.getPath());

        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);

        encoder.setPattern("%d [%thread] %-5level %logger{25} - %msg%n");
        appender.setEncoder(encoder);
        encoder.start();

        final MdcKeyFilter mdcFilter = new MdcKeyFilter(SYSTEM_MDC);
        appender.addFilter(mdcFilter);
        mdcFilter.start();

        // Set filter level to debug.
        final ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setLevel("DEBUG");
        appender.addFilter(thresholdFilter);
        thresholdFilter.start();

        appender.start();

        final ch.qos.logback.classic.Logger logbackLogger
                = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.addAppender(appender);

        return appender;
    }

    private static void destroyAppenders(Appender appender) {
        final LoggerContext loggerContext
                = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger logbackLogger
                = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.detachAppender(appender);
        appender.stop();
    }

    /**
     * Destroy all added appenders.
     */
    public void destroyAll() {
        if (systemAppender != null) {
            destroyAppenders(systemAppender);
            systemAppender = null;
        }
    }

}
