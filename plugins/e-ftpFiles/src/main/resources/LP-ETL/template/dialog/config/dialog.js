define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-ftpFiles#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "passiveMode" : {
            "$type" : "bool",
            "$label" : "Use passive mode"
        },
        "binaryMode" : {
            "$type" : "bool",
            "$label" : "Use binary mode"
        },
        "keepAliveControl" : {
            "$type" : "int",
            "$label" : "Control timeout (seconds)"
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
