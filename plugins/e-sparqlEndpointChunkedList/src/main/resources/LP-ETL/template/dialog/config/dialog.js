define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointChunkedList#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "threads": {
            "$type": "int",
            "$label": "Used threads"
        },
        "timeLimit" : {
            "$type": "int",
            "$label": "Query time limit"
        },
        "encodeRdf": {
            "$type": "bool",
            "$label": "Encode RDF"
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
