define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/t-sparqlSelect#",
        "$type": "Configuration",
        "query" : {
            "$type" : "str",
            "$property" : "query",
            "$control": "queryControl",
            "$label" : "SPARQL SELECT query"
        },
        "fileName" : {
            "$type" : "str",
            "$property" : "fileName",
            "$control": "fileNameControl",
            "$label" : "Output file name"
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
