package com.linkedpipes.etl.storage.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

import java.io.File;

public class LoggerUtils {

    private static final int HISTORY = 7;

    public static Appender<ILoggingEvent> createRollingFileAppender(
            File logDirectory, String logFileName, LoggerContext loggerContext,
            String levelFilter) {
        logDirectory.mkdirs();
        RollingFileAppender appender = new RollingFileAppender();
        appender.setContext(loggerContext);
        appender.setFile(logDirectory.getPath() + File.separator
                + logFileName + ".log");
        addRollingPolicy(appender, loggerContext, logDirectory, logFileName);
        addEncoder(appender, loggerContext,
                "%d [%thread] %-5level %logger{50} - %msg%n");
        addThresholdFilter(appender, levelFilter);
        appender.start();
        return appender;
    }

    public static void addEncoder(
            OutputStreamAppender appender, LoggerContext loggerContext,
            String patter) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(patter);
        appender.setEncoder(encoder);
        encoder.start();
    }


    public static void addThresholdFilter(
            UnsynchronizedAppenderBase appender, String level) {
        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setLevel(level);
        appender.addFilter(thresholdFilter);
        thresholdFilter.start();
    }

    public static void addRollingPolicy(
            RollingFileAppender appender, LoggerContext loggerContext,
            File logDirectory, String fileName) {
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(appender);
        rollingPolicy.setFileNamePattern(logDirectory.getPath()
                + File.separator + fileName + ".%d{yyyy-MM-dd}.%i.log");
        rollingPolicy.setMaxHistory(HISTORY);
        appender.setRollingPolicy(rollingPolicy);
        // File split policy.
        SizeAndTimeBasedFNATP triggeringPolicy;
        triggeringPolicy = new SizeAndTimeBasedFNATP();
        triggeringPolicy.setMaxFileSize(FileSize.valueOf("1024MB"));
        triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
        appender.setTriggeringPolicy(triggeringPolicy);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(
                triggeringPolicy);
        //
        rollingPolicy.start();
        // We need TimeBasedRollingPolicy to have the FileNamePattern
        // pattern initialized which is done in rollingPolicy.start();
        triggeringPolicy.start();
    }

}
