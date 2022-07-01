package com.linkedpipes.etl.executor.component.configuration;

import com.linkedpipes.etl.executor.ExecutorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SubstituteEnvironmentTest {

    @Test
    public void simpleSubstitution() throws ExecutorException {
        Map<String, String> env = new HashMap<>();
        env.put("LP_HOST", "lp");
        env.put("LP_PORT", "8080");
        //
        Assertions.assertEquals("lp:8080", SubstituteEnvironment.substitute(
                env, "{LP_HOST}:{LP_PORT}"));
        Assertions.assertEquals("x-lp:8080", SubstituteEnvironment.substitute(
                env, "x-{LP_HOST}:{LP_PORT}"));
    }

}
