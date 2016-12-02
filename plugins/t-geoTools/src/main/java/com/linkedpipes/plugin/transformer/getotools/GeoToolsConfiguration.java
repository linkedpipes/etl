package com.linkedpipes.plugin.transformer.getotools;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

@RdfToPojo.Type(uri = GeoToolsVocabulary.CONFIG)
public class GeoToolsConfiguration {

    /**
     * Type of entity to load.
     */
    @RdfToPojo.Property(uri = GeoToolsVocabulary.HAS_TYPE)
    private String type;

    /**
     * Predicate with coordinates.
     */
    @RdfToPojo.Property(uri = GeoToolsVocabulary.HAS_COORD)
    private String coord;

    /**
     * Predicate with coordinates type.
     */
    @RdfToPojo.Property(uri = GeoToolsVocabulary.HAS_COORD_TYPE)
    private String coordType;

    /**
     * If default coordinates type if not provided in data.
     */
    @RdfToPojo.Property(uri = GeoToolsVocabulary.HAS_DEFAULT_COORD_TYPE)
    private String defaultCoordType;

    /**
     * Predicate used to connect the new entities to the source
     * entities.
     */
    @RdfToPojo.Property(uri = GeoToolsVocabulary.HAS_OUTPUT_PREDICATE)
    private String outputPredicate;

    /**
     * Output coordinate types.
     */
    @RdfToPojo.Property(uri = GeoToolsVocabulary.HAS_OUTPUT_COORD_TYPE)
    private String outputCoordType;

    @RdfToPojo.Property(uri = GeoToolsVocabulary.HAS_FAIL_ON_ERROR)
    private boolean failOnError = false;

    public GeoToolsConfiguration() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCoord() {
        return coord;
    }

    public void setCoord(String coord) {
        this.coord = coord;
    }

    public String getCoordType() {
        return coordType;
    }

    public void setCoordType(String coordType) {
        this.coordType = coordType;
    }

    public String getDefaultCoordType() {
        return defaultCoordType;
    }

    public void setDefaultCoordType(String defaultCoordType) {
        this.defaultCoordType = defaultCoordType;
    }

    public String getOutputPredicate() {
        return outputPredicate;
    }

    public void setOutputPredicate(String outputPredicate) {
        this.outputPredicate = outputPredicate;
    }

    public String getOutputCoordType() {
        return outputCoordType;
    }

    public void setOutputCoordType(String outputCoordType) {
        this.outputCoordType = outputCoordType;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
}
