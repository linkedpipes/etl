package com.linkedpipes.etl.executor.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ExecutionLogger {

    public static final String EXECUTION_MDC = "execution";

    private FileAppender appender = null;

    public void prepareAppendersForExecution(File logFile, String level) {
        destroyExecutionAppenders();
        appender = createExecutionAppender(logFile, level);
    }

    private FileAppender createExecutionAppender(
            File logFile, String level) {
        LoggerContext loggerContext
                = (LoggerContext) LoggerFactory.getILoggerFactory();

        FileAppender appender = new FileAppender();
        appender.setContext(loggerContext);
        appender.setFile(logFile.getPath());
        LoggerUtils.addEncoder(appender, loggerContext,
                "%d [%thread] %-5level %logger{25} - %msg%n");
        addMdcFilter(appender, EXECUTION_MDC);
        LoggerUtils.addThresholdFilter(appender, level);
        appender.start();

        ch.qos.logback.classic.Logger logbackLogger
                = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.addAppender(appender);

        return appender;
    }

    private void addMdcFilter(
            UnsynchronizedAppenderBase appender, String mdc) {
        MdcKeyFilter mdcFilter = new MdcKeyFilter(mdc);
        appender.addFilter(mdcFilter);
        mdcFilter.start();

    }

    public void destroyExecutionAppenders() {
        if (appender != null) {
            destroyAppender(appender);
            appender = null;
        }
    }


    private static void destroyAppender(FileAppender appender) {
        LoggerContext loggerContext
                = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logbackLogger
                = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        logbackLogger.detachAppender(appender);
        appender.stop();
    }


}
