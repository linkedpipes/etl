package com.linkedpipes.etl.executor.monitor.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.MonitorException;
import com.linkedpipes.etl.executor.monitor.execution.Execution;

import java.util.Collection;

public interface ExecutionSource {

    Collection<Execution> getExecutions();

    Execution getExecution(JsonNode overview) throws MonitorException;

}
