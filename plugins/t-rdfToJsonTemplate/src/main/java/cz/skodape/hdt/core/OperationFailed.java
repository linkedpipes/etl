package cz.skodape.hdt.core;

import org.slf4j.helpers.MessageFormatter;

public class OperationFailed extends Exception {

    private static final long serialVersionUID = 1L;

    protected final String message;

    protected final Object[] args;

    public OperationFailed(String messages, Object... args) {
        this.message = messages;
        this.args = args;
        extractCause(args);
    }

    private void extractCause(Object... args) {
        if (args.length > 0) {
            if (args[args.length - 1] instanceof Throwable) {
                this.initCause((Throwable) args[args.length - 1]);
            }
        }
    }

    @Override
    public String getMessage() {
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }

}
