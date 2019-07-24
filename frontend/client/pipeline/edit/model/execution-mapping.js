/**
 * Define component execution mapping.
 */
((definition) => {
  if (typeof define === "function" && define.amd) {
    define([], definition);
  }
})(() => {
  "use strict";

  const MAPPING_STATUS = {
    /**
     * Finished component with mapping.
     */
    "FINISHED_MAPPED": 0,
    /**
     * Finished component with disabled mapping. Mapping
     * can be enabled.
     */
    "FINISHED": 1,
    /**
     * Failed component with mapping.
     */
    "FAILED_MAPPED": 3,
    /**
     * Failed component with disabled mapping. Mapping
     * can be enabled.
     */
    "FAILED": 6,
    /**
     * Represent an unfinished component with mapping.
     */
    "UNFINISHED": 4,
    /**
     * Represent an unfinished component with disabled mapping. Mapping
     * can be enabled.
     */
    "UNFINISHED_MAPPED": 4,
    /**
     * Changed component, mapping is not available and can not be changed.
     */
    "CHANGED": 5
  };

  return MAPPING_STATUS;

});
