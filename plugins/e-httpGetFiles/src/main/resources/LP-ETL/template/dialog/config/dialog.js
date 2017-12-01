define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-httpGetFiles#",
        "$type": "Configuration",
        "$options" : {
            "$predicate": "auto",
            "$control": "auto"
        },
        "skipOnError" : {
            "$type" : "bool",
            "$label" : "Skip on error"
        },
        "hardRedirect" : {
            "$type" : "bool",
            "$label" : "Force to follow redirects"
        },
        "threads" : {
            "$type": "int",
            "$label": "Number of threads used for download"
        },
        "detailLog" : {
            "$type" : "bool",
            "$label" : "Detail log"
        },
        "timeout" : {
            "$type" : "int",
            "$label" : "Timeout"
        },
        "threadsPerGroup": {
            "$type" : "int",
            "$label" : "Threads per group"
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
