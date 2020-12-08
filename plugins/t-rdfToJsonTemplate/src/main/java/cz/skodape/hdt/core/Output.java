package cz.skodape.hdt.core;

import cz.skodape.hdt.model.OutputConfiguration;

import java.io.IOException;

/**
 * Interface for writing out the data.
 */
public interface Output {

    void openNextArray() throws IOException;

    void closeLastArray() throws IOException;

    void openNextObject() throws IOException;

    void closeLastObject() throws IOException;

    void setNextKey(String key) throws IOException;

    /**
     * Write primitive value.
     *
     * @param configuration Optional configuration object, can be null.
     * @param value         Value to output.
     */
    void writeValue(OutputConfiguration configuration, String value)
            throws IOException;

    /**
     * Called when transformation is finished can be used to flush the
     * content.
     */
    void onTransformationFinished() throws OperationFailed;

}
