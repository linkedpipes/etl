package cz.skodape.hdt.selector.filter;

import cz.skodape.hdt.core.Selector;
import cz.skodape.hdt.model.SelectorConfiguration;
import cz.skodape.hdt.selector.path.PathSelectorConfiguration;

public class FilterSelectorConfiguration implements SelectorConfiguration {

    public enum ConditionType {
        /**
         * At least one value is equal.
         */
        Contain,
        /**
         * There is only one value and it is equal.
         */
        Equal
    }

    public ConditionType condition;

    public String value;

    public PathSelectorConfiguration path;

    @Override
    public Selector createSelector() {
        return new FilterSelector(this);
    }

}
