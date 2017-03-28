define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-jsonToJsonLd#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "context": {
            "$type": "str",
            "$label": "Context"
        },
        "encoding": {
            "$type": "str",
            "$label": "Name of used encoding"
        },
        "fileReference": {
            "$type": "bool",
            "$label" : "Add file reference"
        },
        "dataPredicate": {
            "$type": "iri",
            "$label" : "Data predicate"
        },
        "type": {
            "$type": "iri",
            "$label" : "Root entity type"
        },
        "filePredicate": {
            "$type": "iri",
            "$label" : "File name predicate"
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
