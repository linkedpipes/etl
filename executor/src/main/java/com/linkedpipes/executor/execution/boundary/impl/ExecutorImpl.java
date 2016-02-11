package com.linkedpipes.executor.execution.boundary.impl;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.linkedpipes.executor.Configuration;
import com.linkedpipes.executor.execution.boundary.Executor;
import com.linkedpipes.commons.entities.executor.ExecutionStatus;
import com.linkedpipes.commons.entities.executor.MessageSelectList;
import com.linkedpipes.executor.execution.contoller.PipelineExecutor;
import com.linkedpipes.executor.module.boundary.ModuleFacade;

/**
 *
 * @author Škoda Petr
 */
@Service
public class ExecutorImpl implements Executor {

    @Autowired
    private ModuleFacade moduleFacade;

    @Autowired
    private Configuration configuration;

    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * Store current executor object.
     */
    private PipelineExecutor executor = null;

    @Override
    public ExecutionStatus execute(File directory, String id) {
        if (executor == null) {
            final PipelineExecutor newExecution = new PipelineExecutor(moduleFacade, configuration, id);
            if (newExecution.initialize(directory)) {
                // Set as current executor.
                executor = newExecution;
            }
            // Start execution.
            taskExecutor.execute(() -> {
                executor.execute();
                // Detach execution object once execution finished.
                executor = null;
            });
            // Return status.
            return newExecution.getExecutionStatus();
        } else {
            return null;
        }
    }

    @Override
    public ExecutionStatus getStatus() {
        // Store reference to executor - so we can do null check and then safty call function.
        final PipelineExecutor executorSnapshot = executor;
        if (executorSnapshot == null) {
            return new ExecutionStatus();
        } else {
            return executorSnapshot.getExecutionStatus();
        }
    }

    @Override
    public MessageSelectList getMessages() {
        final PipelineExecutor executorSnapshot = executor;
        if (executorSnapshot == null) {
            return null;
        }
        return executorSnapshot.getMessageStorage().getMessages();
    }

}
