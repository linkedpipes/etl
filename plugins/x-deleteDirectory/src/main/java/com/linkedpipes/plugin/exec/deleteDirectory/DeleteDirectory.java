package com.linkedpipes.plugin.exec.deleteDirectory;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Škoda Petr
 */
public final class DeleteDirectory implements Component.Sequential {

    @Component.Configuration
    public DeleteDirectoryConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        if (configuration.getDirectory() != null) {
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
