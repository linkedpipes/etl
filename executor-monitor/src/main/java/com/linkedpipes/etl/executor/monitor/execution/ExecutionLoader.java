package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.library.rdf.Statements;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

class ExecutionLoader {

    public Statements loadStatements(Execution execution)
            throws MonitorException {
        File file = getExecutionFile(execution);
        if (!file.exists()) {
            return Statements.readOnly(Collections.emptyList());
        }
        try {
            Statements result = Statements.arrayList();
            result.file().addAll(file);
            return result;
        } catch (IOException ex) {
            throw new MonitorException("Can't load execution statements.", ex);
        }
    }

    private File getExecutionFile(Execution execution) {
        return new File(execution.getDirectory(), "execution.trig");
    }

}
