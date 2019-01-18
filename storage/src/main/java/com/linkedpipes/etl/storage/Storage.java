package com.linkedpipes.etl.storage;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

public class Storage {

    private static final int KEEP_LOG_HISTORY_DAYS = 7;

    /**
     * Create rolling file appender with given configuration.
     */
    private static Appender<ILoggingEvent> createRollingFileAppender(
            File logDirectory, String logFileName, LoggerContext loggerContext,
            String levelFilter) {
        logDirectory.mkdirs();

        RollingFileAppender newAppender = new RollingFileAppender();
        newAppender.setContext(loggerContext);
        newAppender.setFile(logDirectory.getPath() + File.separator
                + logFileName + ".log");
        {
            TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
            rollingPolicy.setContext(loggerContext);
            // Rolling policies need to know their parent
            // it's one of the rare cases, where a sub-component
            // knows about its parent.
            rollingPolicy.setParent(newAppender);
            rollingPolicy.setFileNamePattern(logDirectory.getPath()
                    + File.separator
                    + logFileName + ".%d{yyyy-MM-dd}.%i.log");
            rollingPolicy.setMaxHistory(KEEP_LOG_HISTORY_DAYS);
            newAppender.setRollingPolicy(rollingPolicy);
            // File split policy.
            SizeAndTimeBasedFNATP triggeringPolicy;
            {
                triggeringPolicy = new SizeAndTimeBasedFNATP();
                triggeringPolicy.setMaxFileSize(FileSize.valueOf("1024MB"));
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
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d [%thread] %-5level %logger{50} - %msg%n");
        newAppender.setEncoder(encoder);
        encoder.start();
        //
        if (levelFilter != null) {
            ThresholdFilter thresholdFilter = new ThresholdFilter();
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
        Configuration configuration = new Configuration();
        configuration.init();
        File logDirectory = configuration.getLogDirectory();
        LoggerContext loggerContext
                = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logbackLogger
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
        AbstractApplicationContext context
                = new ClassPathXmlApplicationContext(
                "spring/context-storage.xml");
        context.registerShutdownHook();
        context.start();
    }

}
