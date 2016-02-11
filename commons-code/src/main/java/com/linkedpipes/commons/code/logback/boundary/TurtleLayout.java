package com.linkedpipes.commons.code.logback.boundary;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

/**
 *
 * @author Škoda Petr
 */
public class TurtleLayout extends LayoutBase<ILoggingEvent> {

    private static final String HEADER = ""
            + "@prefix log: <http://localhost/ontology/log#>. \n"
            + "\n";

    protected final static DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd");

    protected final static DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    private final String rootResource;

    public TurtleLayout(String rootResource) {
        this.rootResource = rootResource;
    }

    @Override
    public String getFileHeader() {
        final StringBuffer buffer = new StringBuffer(128);
        buffer.append(HEADER);

        return buffer.toString();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        final StringBuffer buffer = new StringBuffer(128);

        // TODO We can utilize prefix for the rootResource.
        buffer.append("<");
        buffer.append(rootResource);
        buffer.append("> log:hasLog [ ");

        buffer.append("log:logger \"");
        buffer.append(event.getLoggerName());
        buffer.append("\";\n");

        buffer.append("log:level \"");
        buffer.append(event.getLevel().levelStr);
        buffer.append("\";\n");

        buffer.append("log:thread \"");
        buffer.append(event.getThreadName());
        buffer.append("\";\n");

        buffer.append("log:time \"");
        final Date date = new Date(event.getTimeStamp());
        buffer.append(DATE_FORMAT.format(date));
        buffer.append("T");
        buffer.append(TIME_FORMAT.format(date));
        buffer.append("\";\n");

        buffer.append("log:message \"\"\"");
        buffer.append(event.getFormattedMessage());
        buffer.append("\"\"\"\n");

        buffer.append("].\n\n");

        return buffer.toString();
    }

}
