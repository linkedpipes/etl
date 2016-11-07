package com.linkedpipes.etl.component.test;

import com.linkedpipes.etl.component.api.service.ProgressReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

final class MockedProgressReport implements ProgressReport {

    private static final Logger LOG
            = LoggerFactory.getLogger(MockedProgressReport.class);

    @Override
    public void start(long entriesToProcess) {
        LOG.info("Progress: start on {} entries", entriesToProcess);
    }

    @Override
    public void start(Collection<?> collection) {
        LOG.info("Progress: start on collection of size {}", collection.size());
    }

    @Override
    public void entryProcessed() {
        LOG.info("Progress: entry");
    }

    @Override
    public void done() {
        LOG.info("Progress: done");
    }

}
