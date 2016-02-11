package com.linkedpipes.executor.logging.boundary;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

/**
 * DENY all messages, whose logger name does not start with one of given prefixes.
 *
 * @author Škoda Petr
 */
public class PackageFilter extends ch.qos.logback.core.filter.Filter<ILoggingEvent> {

    private final String[] packagePrefixes;

    public PackageFilter(String[] packagePrefixes) {
        this.packagePrefixes = packagePrefixes;
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        for (String packagePrefix : packagePrefixes) {
            if (event.getLoggerName().startsWith(packagePrefix)) {
                return FilterReply.NEUTRAL;
            }
        }
        return FilterReply.DENY;
    }

}
