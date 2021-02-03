define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/x-httpRequest#",
        "$type": "Configuration",
        "$options" : {
            "$predicate": "auto",
            "$control": "auto"
        },
        "skipOnError" : {
            "$type" : "bool",
            "$label" : "Skip on error"
        },
        "threads" : {
            "$type": "int",
            "$label": "Number of threads used for download"
        },
        "threadsPerGroup": {
            "$type" : "int",
            "$label" : "Threads per group"
        },
        "followRedirect" : {
            "$type" : "bool",
            "$label" : "Follow redirects"
        },
        "encodeUrl" : {
            "$type": "bool",
            "$label": "Encode input URL"
        },
        "timeout" : {
            "$type" : "int",
            "$label" : "Timeout"
        },
        "utf8Redirect" : {
            "$type": "bool",
            "$label": "Use UTF-8 encoding for redirect"
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
