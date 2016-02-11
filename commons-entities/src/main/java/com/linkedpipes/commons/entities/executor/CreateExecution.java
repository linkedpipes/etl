package com.linkedpipes.commons.entities.executor;

import com.linkedpipes.commons.entities.rest.BaseResponse;
import com.linkedpipes.commons.entities.rest.RestException;

/**
 *
 * @author Škoda Petr
 */
public class CreateExecution extends BaseResponse {

    private String executionId;

    public CreateExecution() {
    }

    public CreateExecution(RestException exception) {
        super(exception);
    }

    public CreateExecution(String executionId) {
        this.executionId = executionId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

}
