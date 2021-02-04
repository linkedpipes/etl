define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointSelect#",
        "$type": "Configuration",
        "$options" : {
            "$predicate": "auto",
            "$control": "auto"
        },
        "query" : {
            "$type" : "str",
            "$label" : "SPARQL CONSTRUCT query"
        },
        "endpoint" : {
            "$type" : "str",
            "$label" : "Endpoint URL"
        },
        "defaultGraph" : {
            "$type": "value",
            "$array": true,
            "$label" : "Default graph"
        },
        "fileName" : {
            "$type" : "str",
            "$label" : "File name"
        },
        "useAuthentication": {
            "$type": "bool",
            "$label": "Use authentication"
        },
        "userName": {
            "$type": "str",
            "$label": "User name"
        },
        "password": {
            "$type": "str",
            "$label": "Password"
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
