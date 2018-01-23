(function () {
    "use-strict";

    const MAPPING_STATUS = {
        /**
         * Finished component with mapping.
         */
        "FINISHED_MAPPED": 0,
        /**
         * Finished component with disabled mapping, can be enabled.
         */
        "FINISHED": 1,
        /**
         * Failed component, it has always the same style.
         */
        "FAILED": 3,
        /**
         * Represent an unfinished component.
         */
        "UNFINISHED": 4,
        /**
         * Changed component, mapping is not available and can not be changed.
         */
        "CHANGED": 5
    };

    const module = {
        "MAPPING_STATUS": MAPPING_STATUS
    };

    if (typeof define === "function" && define.amd) {
        define([], () => module);
    }

})();
