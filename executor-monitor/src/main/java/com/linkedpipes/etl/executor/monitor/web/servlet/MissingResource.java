package com.linkedpipes.etl.executor.monitor.web.servlet;

import com.linkedpipes.etl.executor.monitor.MonitorException;

public class MissingResource extends MonitorException {

    public MissingResource(String message, Object... args) {
        super(message, args);
    }

}
