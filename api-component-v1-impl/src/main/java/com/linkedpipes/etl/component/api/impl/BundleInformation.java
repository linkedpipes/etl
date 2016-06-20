package com.linkedpipes.etl.component.api.impl;

import java.util.List;

/**
 *
 * @author Petr Škoda
 */
class BundleInformation {

    private final Class<?> clazz;

    /**
     * Detected packages.
     */
    private final List<String> packages;

    BundleInformation(Class<?> clazz, List<String> packages) {
        this.clazz = clazz;
        this.packages = packages;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<String> getPackages() {
        return packages;
    }
}
