package cz.skodape.hdt.selector.identity;

import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.Reference;
import cz.skodape.hdt.core.ReferenceSource;
import cz.skodape.hdt.core.Selector;
import cz.skodape.hdt.core.SelectorContext;

/**
 * Just pass the input to output.
 */
class IdentitySelector implements Selector {

    private ReferenceSource input;

    @Override
    public void initialize(SelectorContext context, ReferenceSource input) {
        this.input = input;
    }

    @Override
    public ReferenceSource split() throws OperationFailed {
        IdentitySelector result = new IdentitySelector();
        result.input = this.input.split();
        return result;
    }

    @Override
    public Reference next() throws OperationFailed {
        return this.input.next();
    }

}
