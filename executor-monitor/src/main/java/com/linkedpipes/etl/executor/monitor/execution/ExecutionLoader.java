package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.rdf4j.Statements;

import java.io.File;
import java.io.IOException;

class ExecutionLoader {

    public Statements loadStatements(Execution execution)
            throws MonitorException {
        File file = getExecutionFile(execution);
        if (!file.exists()) {
            return Statements.emptyReadOnly();
        }
        try {
            return Statements.arrayList(file);
        } catch (IOException ex) {
            throw new MonitorException("Can't load execution statements.", ex);
        }
    }

    private File getExecutionFile(Execution execution) {
        return new File(execution.getDirectory(), "execution.trig");
    }

}
