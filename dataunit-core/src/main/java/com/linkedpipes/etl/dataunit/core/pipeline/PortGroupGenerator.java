package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;

import java.util.Map;

interface PortGroupGenerator {

    void generateAndAddPortGroups(Map<String, Port> ports);

    static PortGroupGenerator createGenerator(String rdfRepositoryPolicy) {
        switch (rdfRepositoryPolicy) {
            case LP_PIPELINE.PER_INPUT_REPOSITORY:
                return new PerInputPortGroupGenerator();
            case LP_PIPELINE.SINGLE_REPOSITORY:
            default:
                return new OnePortGroupGenerator();

        }
    }

}
