package cz.skodape.hdt.model;

import cz.skodape.hdt.core.Selector;

public interface SelectorConfiguration {

    /**
     * Create and return instance of selector using this configuration.
     */
    Selector createSelector();

}
