package com.linkedpipes.plugin.exec.deleteDirectory;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public final class DeleteDirectory implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Configuration
    public DeleteDirectoryConfiguration configuration;

    @Override
    public void execute() throws LpException {
        if (configuration.getDirectory() == null) {
            throw new LpException(
                    "Invalid configuration (missing directory).");
        }
        try {
            FileUtils.deleteDirectory(new File(configuration.getDirectory()));
        } catch (IOException ex) {
            throw new LpException("Can't delete directory.", ex);
        }

    }

}
