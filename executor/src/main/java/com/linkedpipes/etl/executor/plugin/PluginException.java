package com.linkedpipes.etl.executor.plugin;

import com.linkedpipes.etl.executor.ExecutorException;

public class PluginException extends ExecutorException {

    public PluginException(String messages, Object... args) {
        super(messages, args);
    }
}
