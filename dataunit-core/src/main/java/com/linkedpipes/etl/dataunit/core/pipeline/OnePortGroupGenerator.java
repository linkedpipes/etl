package com.linkedpipes.etl.dataunit.core.pipeline;

import java.util.Map;

class OnePortGroupGenerator implements PortGroupGenerator {

    static final Integer DEFAULT_GROUP = 0;

    @Override
    public void generateAndAddPortGroups(Map<String, Port> ports) {
        for (Port port : ports.values()) {
            port.setGroup(DEFAULT_GROUP);
        }
    }

}
