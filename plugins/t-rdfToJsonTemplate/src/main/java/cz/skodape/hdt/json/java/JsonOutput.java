package cz.skodape.hdt.json.java;

import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.Output;
import cz.skodape.hdt.model.OutputConfiguration;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Stack;

public class JsonOutput implements Output {

    private static class State {

        public boolean writeSeparator = false;

    }

    private final Writer writer;

    private final Stack<State> states = new Stack<>();

    private final boolean prettyPrint;

    private String key = null;

    private StringBuilder prefix = new StringBuilder();

    public JsonOutput(Writer writer, boolean prettyPrint) {
        this.writer = writer;
        this.prettyPrint = prettyPrint;
    }

    @Override
    public void openNextArray() throws IOException {
        this.writeSeparator();
        this.writeIndentation();
        this.writeKey();
        this.writer.write("[");
        this.states.add(new State());
        this.increaseIndentation();
    }

    private void writeIndentation() throws IOException {
        if (this.prettyPrint) {
            this.writer.write(prefix.toString());
        }
    }

    private void increaseIndentation() {
        if (this.prettyPrint) {
            this.prefix.append("  ");
        }
    }

    private void writeSeparator() throws IOException {
        if (this.states.empty()) {
            return;
        }
        var lastState = this.states.peek();
        if (lastState.writeSeparator) {
            this.writer.write(",");
        }
        if (this.prettyPrint) {
            this.writer.write("\n");
        }
        lastState.writeSeparator = true;
    }

    private void writeKey() throws IOException {
        if (this.key == null) {
            return;
        }
        this.writer.write("\"");
        this.writer.write(this.key);
        this.writer.write("\":");
        this.key = null;
    }

    @Override
    public void closeLastArray() throws IOException {
        this.decreaseIndentation();
        if (this.prettyPrint) {
            this.writer.write("\n");
        }
        this.writeIndentation();
        this.writer.write("]");
        this.states.pop();
        this.key = null;
    }

    private void decreaseIndentation() {
        if (this.prettyPrint) {
            this.prefix.setLength(this.prefix.length() - 2);
        }
    }

    @Override
    public void openNextObject() throws IOException {
        this.writeSeparator();
        this.writeIndentation();
        this.writeKey();
        this.writer.write("{");
        this.states.add(new State());
        this.increaseIndentation();
    }

    @Override
    public void closeLastObject() throws IOException {
        this.decreaseIndentation();
        this.states.pop();
        if (this.prettyPrint) {
            this.writer.write("\n");
        }
        this.writeIndentation();
        this.writer.write("}");
        this.key = null;
    }

    @Override
    public void setNextKey(String key) {
        this.key = key;
    }

    @Override
    public void writeValue(OutputConfiguration configuration, String value)
            throws IOException {
        this.writeSeparator();
        this.writeIndentation();
        this.writeKey();
        if (!(configuration instanceof JsonOutputConfiguration)) {
            throw new IOException("Invalid configuration instance.");
        }
        var jsonConfiguration = (JsonOutputConfiguration) configuration;
        switch (jsonConfiguration.datatype) {
            case Boolean:
                this.writeBoolean(value);
                break;
            case String:
                this.writeString(value);
                break;
            case Number:
                this.writeNumber(value);
                break;
            default:
                throw new IOException(
                        "Unknown JSON output type: "
                                + jsonConfiguration.datatype);
        }
    }

    private void writeBoolean(String value) throws IOException {
        if ("1".equals(value)) {
            value = "true";
        }
        if ("0".equals(value)) {
            value = "false";
        }
        writer.write(value.toLowerCase(Locale.ROOT));
    }

    private void writeString(String value) throws IOException {
        String sanitizedValue = value
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\"", "\\\"");
        writer.write("\"");
        writer.write(sanitizedValue);
        writer.write("\"");
    }

    private void writeNumber(String value) throws IOException {
        String sanitizedValue = value.replace(",", ".").replace(" ", "");
        writer.write(sanitizedValue);
    }

    @Override
    public void onTransformationFinished() throws OperationFailed {
        try {
            this.writer.flush();
        } catch (IOException ex) {
            throw new OperationFailed("Can't flush content.", ex);
        }
    }
}
