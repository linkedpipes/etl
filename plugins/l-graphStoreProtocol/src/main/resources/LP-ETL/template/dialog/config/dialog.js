define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/l-graphStoreProtocol#",
        "$type": "Configuration",
        "$control": {
            "$predicate": "auto"
        },
        "graph": {
            "$type": "str",
            "$label": "Target graph IRI"
        },
        "repository": {
            "$type": "str",
            "$label": "Repository"
        },
        "authentification": {
            "$type": "bool",
            "$label": "Use authentication"
        },
        "user": {
            "$type": "str",
            "$label": "User name"
        },
        "password": {
            "$type": "str",
            "$label": "Password"
        },
        "checkSize": {
            "$type": "bool",
            "$label": "Log graph size"
        },
        "endpointSelect": {
            "$type": "str",
            "$label": "SPARQL Endpoint"
        },
        "endpoint": {
            "$type": "str",
            "$label": "Graph store protocol endpoint"
        },
        "replace": {
            "$type": "bool",
            "$label": "Clear target graph before loading"
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
