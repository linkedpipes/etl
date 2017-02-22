package com.linkedpipes.plugin.exec.virtuoso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

class LoadWorker implements Callable<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(LoadWorker.class);

    private final SqlExecutor sqlExecutor;

    private Exception exception = null;

    public LoadWorker(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    @Override
    public Object call() {
        LOG.info("Loading ...");
        try {
            sqlExecutor.loadData();
        } catch (Exception ex) {
            exception = ex;
            LOG.error("Loading failed.", ex);
        }
        LOG.info("Loading ... done");
        return null;
    }

    public Exception getException() {
        return exception;
    }
}
