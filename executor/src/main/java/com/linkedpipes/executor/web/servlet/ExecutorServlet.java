package com.linkedpipes.executor.web.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.linkedpipes.commons.entities.rest.RestException;
import com.linkedpipes.commons.entities.executor.CreateExecution;
import com.linkedpipes.commons.entities.executor.ExecutionStatus;
import com.linkedpipes.commons.entities.executor.MessageSelectList;
import com.linkedpipes.executor.execution.boundary.Executor;

/**
 *
 * @author Škoda Petr
 */
@RestController
@RequestMapping(value = "/executions")
public class ExecutorServlet {

    /**
     * Data transfer object for incoming task definition.
     */
    public static class TaskDefinition {

        /**
         * Directory with execution.
         */
        public String directory;

        /**
         * Execution id.
         */
        public String executionId;

    };

    @Autowired
    private Executor executor;

    @RequestMapping(value = "", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateExecution accept(@RequestBody TaskDefinition task, HttpServletResponse response) throws IOException {
        final ExecutionStatus status = executor.execute(new File(task.directory), task.executionId);
        if (status == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return new CreateExecution(new RestException(
                    "",
                    "",
                    "Another pipeline is already running!",
                    RestException.Codes.ERROR));
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            return new CreateExecution(status.getId());
        }
    }

    @RequestMapping(value = "/messages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MessageSelectList queryMessages() {
        return executor.getMessages();
    }

}
