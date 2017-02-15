package com.linkedpipes.etl.executor;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import com.linkedpipes.etl.executor.logging.LoggerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

public class Executor {

    private static void initLogger() {
        final Configuration configuration = new Configuration();
        configuration.init();
        final File logDirectory = configuration.getLogDirectory();
        final LoggerContext loggerContext
                = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger logbackLogger
                = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        //
        Appender appender = LoggerFacade.createRollingFileAppender(
                new File(logDirectory, "executor"), "executor", loggerContext,
                configuration.getLogCoreFilter());
        logbackLogger.addAppender(appender);
    }

    private static void startExecutorService() {
        initLogger();
        final ConfigurableApplicationContext context
                = new ClassPathXmlApplicationContext(
                "spring/context-service.xml");
        context.registerShutdownHook();
        context.start();
    }

    public static void main(String[] args) {
        startExecutorService();
    }

}
