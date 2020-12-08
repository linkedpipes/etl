package cz.skodape.hdt.selector.once;

import cz.skodape.hdt.core.Selector;
import cz.skodape.hdt.model.SelectorConfiguration;

public class OnceSelectorConfiguration implements SelectorConfiguration {

    @Override
    public Selector createSelector() {
        return new OnceSelector();
    }

}
