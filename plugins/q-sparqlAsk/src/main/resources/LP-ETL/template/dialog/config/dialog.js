define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/q-sparqlAsk#",
        "$type": "Configuration",
        "$control": {
            "$predicate": "auto"
        },
        "query": {
            "$type": "str",
            "$label": "Query"
        },
        "failOnTrue": {
            "$type": "bool",
            "$label": "Target graph IRI"
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
