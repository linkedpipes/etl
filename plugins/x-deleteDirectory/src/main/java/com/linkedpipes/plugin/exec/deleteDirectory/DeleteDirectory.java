package com.linkedpipes.plugin.exec.deleteDirectory;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public final class DeleteDirectory implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.Configuration
    public DeleteDirectoryConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getDirectory() == null) {
            throw exceptionFactory.failure(
                    "Invalid configuration (missing directory).");
        }
        try {
            FileUtils.deleteDirectory(new File(configuration.getDirectory()));
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't delete directory.", ex);
        }

    }

}
