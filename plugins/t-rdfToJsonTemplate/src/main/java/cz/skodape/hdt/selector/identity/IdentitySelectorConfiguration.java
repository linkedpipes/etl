package cz.skodape.hdt.selector.identity;

import cz.skodape.hdt.core.Selector;
import cz.skodape.hdt.model.SelectorConfiguration;

public class IdentitySelectorConfiguration implements SelectorConfiguration {

    @Override
    public Selector createSelector() {
        return new IdentitySelector();
    }

}
