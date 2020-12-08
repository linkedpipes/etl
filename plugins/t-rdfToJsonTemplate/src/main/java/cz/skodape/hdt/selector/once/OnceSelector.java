package cz.skodape.hdt.selector.once;

import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.Reference;
import cz.skodape.hdt.core.ReferenceSource;
import cz.skodape.hdt.core.Selector;
import cz.skodape.hdt.core.SelectorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnceSelector implements Selector {

    private static final Logger LOG =
            LoggerFactory.getLogger(OnceSelector.class);

    private ReferenceSource input = null;

    private Reference returnedReference = null;

    @Override
    public void initialize(SelectorContext context, ReferenceSource input) {
        this.input = input;
    }

    @Override
    public ReferenceSource split() {
        OnceSelector result = new OnceSelector();
        result.input = input;
        result.returnedReference = returnedReference;
        return result;
    }

    @Override
    public Reference next() throws OperationFailed {
        if (returnedReference != null) {
            logPossibleContent();
            return null;
        }
        returnedReference = input.next();
        return returnedReference;
    }

    protected void logPossibleContent() throws OperationFailed {
        Reference next = input.next();
        if (next == null) {
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("Only one value expected but got:\n  ");
        message.append(returnedReference.asDebugString());
        do {
            message.append("\n  ");
            message.append(next.asDebugString());
        } while ((next = input.next()) != null);
        LOG.warn(message.toString());
    }

}
