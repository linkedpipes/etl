define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/t-modifyDate#",
        "$type": "Configuration",
        "input" : {
            "$type" : "iri",
            "$property" : "input",
            "$control": "inputControl",
            "$label" : "Input predicate"
        },
        "shiftBy" : {
            "$type" : "int",
            "$property" : "shiftBy",
            "$control": "shiftByControl",
            "$label" : "Shift date by"
        },
        "output" : {
            "$type" : "iri",
            "$property" : "output",
            "$control": "outputControl",
            "$label" : "Output predicate"
        }
    };

    function controller($scope, $service) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

        const dialogManager = $service.v1.manager(DESC, $scope.dialog);

        $service.onStore = function () {
            dialogManager.save();
        };

        dialogManager.load();

    }

    controller.$inject = ['$scope', '$service'];
    return controller;
});
