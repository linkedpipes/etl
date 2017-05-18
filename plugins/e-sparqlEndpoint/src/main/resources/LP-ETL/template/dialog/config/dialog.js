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
        },
        "defaultGraph" : {
            "$type": "value",
            "$array": true,
            "$property" : "defaultGraph",
            "$control": "defaultGraphControl",
            "$label" : "Default graph"
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

        DESC.defaultGraph["$onSave"] = $service.v1.fnc.removeEmptyIri;

        const dialogManager = $service.v1.manager(DESC, $scope.dialog);

        $service.onStore = function () {
            dialogManager.save();
        };

        dialogManager.load();

    }

    controller.$inject = ['$scope', '$service'];
    return controller;
});
