package com.linkedpipes.etl.executor.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * DENY all messages, whose logger's name does not start with given prefixes.
 */
class PackageFilter extends Filter<ILoggingEvent> {

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
