package com.linkedpipes.etl.dpu.test;

import java.io.File;
import com.linkedpipes.etl.component.api.Component;

/**
 *
 * @author Petr Škoda
 */
final class TestContext implements Component.Context {

    private final String componentUri;

    private final File workingDirectory;

    TestContext(String componentUri, File workingDirectory) {
        this.componentUri = componentUri;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public boolean canceled() {
        return false;
    }

    @Override
    public String getComponentIri() {
        return componentUri;
    }

    @Override
    public File getWorkingDirectory() {
        return workingDirectory;
    }

}
