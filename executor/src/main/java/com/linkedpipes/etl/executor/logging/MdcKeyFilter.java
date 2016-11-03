package com.linkedpipes.etl.executor.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Filter messages that does not contains given key in MDC context.
 */
class MdcKeyFilter extends Filter<ILoggingEvent> {

    /**
     * Key to filter for.
     */
    private final String key;

    public MdcKeyFilter(String value) {
        this.key = value;
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getMDCPropertyMap().containsKey(key)) {
            return FilterReply.NEUTRAL;
        } else {
            return FilterReply.DENY;
        }
    }

}
