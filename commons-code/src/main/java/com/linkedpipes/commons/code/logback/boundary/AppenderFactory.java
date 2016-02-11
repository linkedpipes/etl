package com.linkedpipes.commons.code.logback.boundary;

import java.io.File;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

/**
 *
 * @author Škoda Petr
 */
public final class AppenderFactory {

    public static Appender<ILoggingEvent> createRollingFileAppender(File logDirectory, String logFileName,
            LoggerContext loggerContext, String levelFilter) {
        final File coreLogDirectory = new File(logDirectory, "core");
        coreLogDirectory.mkdirs();

        final RollingFileAppender newAppender = new RollingFileAppender();
        newAppender.setContext(loggerContext);
        newAppender.setFile(coreLogDirectory.getPath() + File.separator + logFileName + ".log");
        {
            final TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
            rollingPolicy.setContext(loggerContext);
            // Rolling policies need to know their parent
            // it's one of the rare cases, where a sub-component knows about its parent.
            rollingPolicy.setParent(newAppender);
            rollingPolicy.setFileNamePattern(coreLogDirectory.getPath() + File.separator
                    + logFileName + ".%d{yyyy-MM-dd}.%i.log");
            rollingPolicy.setMaxHistory(7);
            newAppender.setRollingPolicy(rollingPolicy);
            // File split policy.
            final SizeAndTimeBasedFNATP triggeringPolicy;
            {
                triggeringPolicy = new SizeAndTimeBasedFNATP();
                triggeringPolicy.setMaxFileSize("64MB");
                triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
                newAppender.setTriggeringPolicy(triggeringPolicy);
            }
            rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
            rollingPolicy.start();
            // We need TimeBasedRollingPolicy to have the FileNamePattern pattern initialized which is
            // done in rollingPolicy.start();
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

}
