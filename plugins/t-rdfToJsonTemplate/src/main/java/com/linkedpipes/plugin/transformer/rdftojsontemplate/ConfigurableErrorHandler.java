package com.linkedpipes.plugin.transformer.rdftojsontemplate;

import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.Reference;
import cz.skodape.hdt.core.ReferenceSource;
import cz.skodape.hdt.core.TransformErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurableErrorHandler extends TransformErrorHandler {

    private static final Logger LOG =
            LoggerFactory.getLogger(ConfigurableErrorHandler.class);

    private boolean ignoreMultiplePrimitives = false;

    @Override
    public void onMultiplePrimitiveValues(
            Reference head, Reference next,
            ReferenceSource source) throws OperationFailed {
        String message = multiplePrimitiveValuesMessage(head, next, source);
        if (ignoreMultiplePrimitives) {
            LOG.warn("Ignoring multiple values for primitive: {}", message);
        } else {
            throw new OperationFailed(
                    "Multiple values detected for primitive: {}", message);
        }
    }

    public void setIgnoreMultiplePrimitives(boolean ignoreMultiplePrimitives) {
        this.ignoreMultiplePrimitives = ignoreMultiplePrimitives;
    }

}
