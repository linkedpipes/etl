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
        Configuration configuration = new Configuration();
        configuration.init();
        File logDirectory = configuration.getLogDirectory();
        LoggerContext loggerContext
                = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logbackLogger
                = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        String logLevel = configuration.getLogCoreFilter();
        //
        Appender appender = LoggerFacade.createRollingFileAppender(
                new File(logDirectory, "executor"), "executor",
                loggerContext, logLevel);
        logbackLogger.addAppender(appender);
    }

    private static void startExecutorService() {
        initLogger();
        ConfigurableApplicationContext context
                = new ClassPathXmlApplicationContext(
                "spring/context-service.xml");
        context.registerShutdownHook();
        context.start();
    }

    public static void main(String[] args) {
        startExecutorService();
    }

}
