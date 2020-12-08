package cz.skodape.hdt.selector.path;

import cz.skodape.hdt.core.Selector;
import cz.skodape.hdt.model.SelectorConfiguration;

import java.util.ArrayList;
import java.util.List;

public class PathSelectorConfiguration implements SelectorConfiguration {

    public static class Path {

        public String predicate;

        public boolean reverse = false;

    }

    public List<Path> path = new ArrayList<>();

    @Override
    public Selector createSelector() {
        return new PathSelector(this);
    }   

}
