package com.linkedpipes.etl.storage;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 *
 * @author Petr Škoda
 */
public class Storage {

    /**
     * Create rolling file appender with given configuration.
     *
     * @param logDirectory
     * @param logFileName
     * @param loggerContext
     * @param levelFilter
     * @return
     */
    private static Appender<ILoggingEvent> createRollingFileAppender(
            File logDirectory, String logFileName, LoggerContext loggerContext,
            String levelFilter) {
        logDirectory.mkdirs();

        final RollingFileAppender newAppender = new RollingFileAppender();
        newAppender.setContext(loggerContext);
        newAppender.setFile(logDirectory.getPath() + File.separator
                + logFileName + ".log");
        {
            final TimeBasedRollingPolicy rollingPolicy
                    = new TimeBasedRollingPolicy();
            rollingPolicy.setContext(loggerContext);
            // Rolling policies need to know their parent
            // it's one of the rare cases, where a sub-component
            // knows about its parent.
            rollingPolicy.setParent(newAppender);
            rollingPolicy.setFileNamePattern(logDirectory.getPath()
                    + File.separator
                    + logFileName + ".%d{yyyy-MM-dd}.%i.log");
            rollingPolicy.setMaxHistory(7);
            newAppender.setRollingPolicy(rollingPolicy);
            // File split policy.
            final SizeAndTimeBasedFNATP triggeringPolicy;
            {
                triggeringPolicy = new SizeAndTimeBasedFNATP();
                triggeringPolicy.setMaxFileSize("1024MB");
                triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
                newAppender.setTriggeringPolicy(triggeringPolicy);
            }
            rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(
                    triggeringPolicy);
            rollingPolicy.start();
            // We need TimeBasedRollingPolicy to have the FileNamePattern
            // pattern initialized which is done in rollingPolicy.start();
            triggeringPolicy.start();
        }
        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d [%thread] %-5level %logger{50} - %msg%n");
        newAppender.setEncoder(encoder);
        encoder.start();
        //
        if (levelFilter != null) {
            final ThresholdFilter thresholdFilter = new ThresholdFilter();
            thresholdFilter.setLevel(levelFilter);
            newAppender.addFilter(thresholdFilter);
            thresholdFilter.start();
        }
        // ...
        newAppender.start();
        return newAppender;
    }

    /**
     * Initialize system loggers, before any other part
     * of the application starts.
     */
    private static void initLogger() {
        final Configuration configuration = new Configuration();
        configuration.init();
        final File logDirectory = configuration.getLogDirectory();
        final LoggerContext loggerContext
                = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger logbackLogger
                = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        //
        logbackLogger.addAppender(createRollingFileAppender(
                new File(logDirectory, "storage"),
                "storage",
                loggerContext,
                configuration.getLogCoreFilter()));
    }

    public static void main(String[] args) {
        initLogger();
        final AbstractApplicationContext context
                = new ClassPathXmlApplicationContext(
                        "spring/context-storage.xml");
        context.registerShutdownHook();
        context.start();
    }

}
