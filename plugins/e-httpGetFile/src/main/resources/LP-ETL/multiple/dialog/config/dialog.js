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
        },
        "encodeUrl": {
            "$type" : "bool",
            "$label" : "Encode input URL"
        },
        "utf8Redirect": {
            "$type" : "bool",
            "$label" : "Use UTF-8 encoding for redirect"
        },
        "retryCount": {
            "$type" : "int",
            "$label" : "Retry count"
        },
        "waitTime":{
            "$type" : "int",
            "$label" : "Wait time between downloads"
        },
        "retryWaitTime":{
            "$type" : "int",
            "$label" : "Wait time between retry"
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
