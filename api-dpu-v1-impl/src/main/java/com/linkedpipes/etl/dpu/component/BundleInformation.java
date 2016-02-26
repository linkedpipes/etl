package com.linkedpipes.etl.dpu.component;

import java.util.List;

/**
 * Holds information about a single bundle.
 *
 * @author Škoda Petr
 */
final class BundleInformation {

    /**
     * Classes that implements {@link cz.cuni.mff.xrg.cuv.component.Component}.
     */
    private final Class<?> componentClasse;

    /**
     * Packages that were detected in the bundle.
     */
    private final List<String> packages;

    public BundleInformation(Class<?> componentClasse, List<String> packages) {
        this.componentClasse = componentClasse;
        this.packages = packages;
    }

    public Class<?> getComponentClasse() {
        return componentClasse;
    }

    public List<String> getPackages() {
        return packages;
    }

}
