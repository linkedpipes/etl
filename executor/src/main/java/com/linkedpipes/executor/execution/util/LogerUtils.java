package com.linkedpipes.executor.execution.util;

import java.io.File;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.Headers;
import com.linkedpipes.executor.execution.entity.PipelineConfiguration;
import com.linkedpipes.executor.logging.boundary.MdcFilter;
import com.linkedpipes.executor.logging.boundary.MdcValue;
import com.linkedpipes.executor.logging.boundary.PackageFilter;
import com.linkedpipes.commons.code.logback.boundary.TurtleLayout;

/**
 * TODO Replace this class with AppenderFactory .. or enable configuration from RDF.
 *
 * @author Škoda Petr
 */
public final class LogerUtils {

    /**
     * Create system appender.
     *
     * @param context
     * @param targetFile
     */
    public static Appender<ILoggingEvent> createSystemAppender(File targetFile) {
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        final FileAppender appender = new FileAppender();
        appender.setContext(loggerContext);
        appender.setFile(targetFile.getPath());

        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);

        encoder.setPattern("%d [%thread] %-5level %logger{25} - %msg%n");
        appender.setEncoder(encoder);
        encoder.start();

        final MdcFilter mdcFilter = new MdcFilter(MdcValue.SYSTEM_FLAG);
        appender.addFilter(mdcFilter);
        mdcFilter.start();

        appender.start();
        logbackLogger.addAppender(appender);
        return appender;
    }

    public static void destroyAppenders(Collection<Appender<ILoggingEvent>> appenders) {
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        for (Appender<ILoggingEvent> appender : appenders) {
            logbackLogger.detachAppender(appender);
            appender.stop();
        }
    }

    public static Appender<ILoggingEvent> createComponentAppenders(File targetFile,
            PipelineConfiguration.Component component, Component componentInstance) {
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        final FileAppender appender = new FileAppender();
        appender.setContext(loggerContext);
        appender.setFile(targetFile.getPath());

        final LayoutWrappingEncoder encoder = new LayoutWrappingEncoder();
        encoder.setContext(loggerContext);
        encoder.setLayout(new TurtleLayout(component.getUri()));
        appender.setEncoder(encoder);
        encoder.start();

        // MDC filter.
        final MdcFilter mdcFilter = new MdcFilter(MdcValue.COMPONENT_FLAG);
        appender.addFilter(mdcFilter);
        mdcFilter.start();
        // Package filter if possible.
        final String logPackages = componentInstance.getHeader(Headers.LOG_PACKAGES);
        if (logPackages != null) {
            String[] packages = logPackages.split(",");
            final PackageFilter packageFilter = new PackageFilter(packages);
            appender.addFilter(packageFilter);
            packageFilter.start();
        }
        appender.start();
        logbackLogger.addAppender(appender);
        return appender;
    }

}
