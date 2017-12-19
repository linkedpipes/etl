/**
 * Contains definition of the LP vocabulary.
 */
(function () {
    "use strict";

    // LinkedPipes core vocabulary.
    const vocabulary = {};

    const dataUnitPrefix = "http://linkedpipes.com/ontology/dataUnit/";

    vocabulary.DataUnitSesameChunked =
        dataUnitPrefix + "sesame/1.0/rdf/Chunked";

    vocabulary.DataUnitSesameSingleGraph =
        dataUnitPrefix + "sesame/1.0/rdf/SingleGraph";

    // Information about data unit compatibility.
    const dataunitCompatibility = {};

    /**
     * Check if given source can be connected to given target port.
     */
    function compatible(source, target) {
        if (source === target) {
            return true;
        }
        return dataunitCompatibility[source] !== undefined &&
            dataunitCompatibility[source][target] === true;
    }

    const module = {
        "vocabulary": vocabulary,
        "dataunit": {
            "compatible": compatible
        }
    };

    if (typeof define === "function" && define.amd) {
        define([], () => module);
    }

    if (typeof module !== "undefined") {
        module.exports = module;
    }
})();
