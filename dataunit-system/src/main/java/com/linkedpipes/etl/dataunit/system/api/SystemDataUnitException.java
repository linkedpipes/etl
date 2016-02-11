package com.linkedpipes.etl.dataunit.system.api;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;

/**
 *
 * @author Škoda Petr
 */
public class SystemDataUnitException extends NonRecoverableException {

    public SystemDataUnitException(String message) {
        super(message);
    }

    public SystemDataUnitException(String message, Throwable cause) {
        super(message, cause);
    }

}
