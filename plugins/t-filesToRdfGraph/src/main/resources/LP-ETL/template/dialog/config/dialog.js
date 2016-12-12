define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/t-filesToRdf#",
        "$type": "Configuration",
        "$options" : {
            "$predicate": "auto",
            "$control": "auto"
        },
        "mimeType" : {
            "$type" : "str",
            "$label" : "Format",
            "$onLoad": (value) => {
                if (value === "") {
                    return "null";
                } else {
                    return value;
                }
            },
            "$onSave": (value) => {
                if (value === "null") {
                    return "";
                } else {
                    return value;
                }
            }
        },
        "softFail" : {
            "$type" : "bool",
            "$label" : "Skip file on failure"
        },
        "commitSize" : {
            "$type" : "int",
            "$label" : "Commit size"
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
