package com.linkedpipes.etl.dataunit.system.api;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.util.Arrays;

/**
 *
 * @author Škoda Petr
 */
public class SystemDataUnitException extends NonRecoverableException {

    public SystemDataUnitException(String messages, Object... args) {
        super(Arrays.asList(new Message(messages, "en")), args);
    }

}
