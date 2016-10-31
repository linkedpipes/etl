define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-sparqlEndpoint#",
        "$type": "Configuration",
        "query" : {
            "$type" : "str",
            "$property" : "query",
            "$control": "queryControl",
            "$label" : "Endpoint URL"
        },
        "endpoint" : {
            "$type" : "str",
            "$property" : "endpoint",
            "$control": "endpointControl",
            "$label" : "SPARQL CONSTRUCT query"
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
