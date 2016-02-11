package com.linkedpipes.executor;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.qos.logback.classic.LoggerContext;
import com.linkedpipes.commons.code.logback.boundary.AppenderFactory;

/**
 *
 * @author Škoda Petr
 */
public final class Executor {

    protected static void initLoggers() {
        final Configuration configuration = new Configuration();
        configuration.init();
        final File logDirectory = configuration.getLogDirectory();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        logbackLogger.addAppender(AppenderFactory.createRollingFileAppender(
                new File(logDirectory, "executor"),
                "core",
                loggerContext,
                configuration.getLogCoreFilter()));
    }

    public static void main(String[] args) {
        // We need to initialize logging before spring to get all the logs.
        initLoggers();
        // Start application context.
        final AbstractApplicationContext context
                = new ClassPathXmlApplicationContext("spring/context-executor.xml");
        context.registerShutdownHook();
        context.start();
    }

}
