package com.linkedpipes.etl.dpu.test;

import com.linkedpipes.etl.dpu.api.DataProcessingUnit;
import java.io.File;

/**
 *
 * @author Petr Škoda
 */
final class TestContext implements DataProcessingUnit.Context {

    private final String componentUri;

    private final File workingDirectory;

    public TestContext(String componentUri, File workingDirectory) {
        this.componentUri = componentUri;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public boolean canceled() {
        return false;
    }

    @Override
    public String getComponentUri() {
        return componentUri;
    }

    @Override
    public File getWorkingDirectory() {
        return workingDirectory;
    }

}
