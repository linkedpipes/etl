package com.linkedpipes.etl.executor.monitor.execution;

import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.debug.DebugData;
import com.linkedpipes.etl.rdf4j.Statements;

import java.io.File;
import java.io.IOException;

class ExecutionLoader {

    public void loadFromDirectory(Execution execution) throws MonitorException {
        Statements statements = this.loadStatements(execution);
        execution.setDebugData(new DebugData(statements, execution));
    }

    public Statements loadStatements(Execution execution)
            throws MonitorException {
        File file = this.getExecutionFile(execution);
        Statements statements = Statements.ArrayList();
        try {
            statements.addAll(file);
            return statements;
        } catch (IOException ex) {
            throw new MonitorException("Can't load execution statements.", ex);
        }
    }

    private File getExecutionFile(Execution execution) {
        return new File(execution.getDirectory(), "execution.trig");
    }

}
