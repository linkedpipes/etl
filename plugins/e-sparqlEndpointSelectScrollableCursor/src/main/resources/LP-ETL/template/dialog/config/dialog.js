define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointSelectScrollableCursor#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "endpoint": {
            "$type": "str",
            "$label": "Endpoint"
        },
        "fileName": {
            "$type": "str",
            "$label": "File name"
        },
        "pageSize": {
            "$type": "int",
            "$label": "Page size"
        },
        "defaultGraph": {
            "$type": "value",
            "$array": true,
            "$label": "Default graphs"
        },
        "prefixes": {
            "$type": "str",
            "$label": "Query prefixes"
        },
        "outerSelect": {
            "$type": "str",
            "$label": "Outer select clause"
        },
        "innerSelect": {
            "$type": "str",
            "$label": "Inner select query"
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
