define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointChunked#",
        "$type": "Configuration",
        "endpoint" : {
            "$type" : "str",
            "$property" : "endpoint",
            "$control": "endpointControl",
            "$label" : "SPARQL CONSTRUCT query"
        },
        "headerAccept" : {
            "$type" : "str",
            "$property" : "headerAccept",
            "$control": "headerAcceptControl",
            "$label" : "Used MimeType"
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
