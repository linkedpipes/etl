define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointConstructScrollableCursor#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "endpoint": {
            "$type": "str",
            "$label": "Endpoint"
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
        "outerConstruct": {
            "$type": "str",
            "$label": "Outer construct clause"
        },
        "innerSelect": {
            "$type": "str",
            "$label": "Inner select query"
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
