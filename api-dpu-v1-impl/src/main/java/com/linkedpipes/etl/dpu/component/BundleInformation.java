package com.linkedpipes.etl.dpu.component;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
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

    /**
     * Map of detected property files, stored under language key.
     */
    private final Map<String, ResourceBundle> localizationFiles;

    public BundleInformation(
            Class<?> componentClasse, List<String> packages, Map<String, ResourceBundle> propertyFiles) {
        this.componentClasse = componentClasse;
        this.packages = packages;
        this.localizationFiles = propertyFiles;
    }

    public Class<?> getComponentClasse() {
        return componentClasse;
    }

    public List<String> getPackages() {
        return packages;
    }

    public Map<String, ResourceBundle> getLocalizationFiles() {
        return localizationFiles;
    }

}
