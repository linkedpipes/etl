package com.linkedpipes.executor.logging.boundary;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Filter for presence of MDC value.
 * 
 * @author Škoda Petr
 */
public class MdcFilter extends ch.qos.logback.core.filter.Filter<ILoggingEvent> {

    private final String key;

    public MdcFilter(String key) {
        this.key = key;
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
