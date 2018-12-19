define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-jsonLdToRdfChunked#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "softFail": {
            "$type": "bool",
            "$label": "Skip file on failure"
        },
        "commitSize": {
            "$type": "int",
            "$label": "Files per chunk"
        },
        "fileReference": {
            "$type": "bool",
            "$label" : "Add file reference"
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
