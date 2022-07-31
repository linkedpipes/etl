package com.linkedpipes.etl.executor.plugin;

import com.linkedpipes.etl.executor.ExecutorException;

public class BannedComponent extends ExecutorException {

    public BannedComponent(Object... args) {
        super("Required component '{}' is banned by '{}'.", args);
    }

}
