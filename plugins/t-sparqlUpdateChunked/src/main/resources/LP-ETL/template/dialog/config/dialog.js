define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-sparqlUpdateChunked#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "query": {
            "$type": "str",
            "$label": "SPARQL UPDATE query"
        },
        "threads" : {
            "$type": "int",
            "$label": "Number of threads to use"
        },
        "softFail": {
            "$type": "bool",
            "$label": "Skip file on failure"
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
