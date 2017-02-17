define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/x-deleteDirectory#",
        "$type": "Configuration",
        "$control" : "control",
        "$options" : {
            "$predicate": "auto"
        },
        "directory" : {
            "$type" : "str",
            "$label" : "Directory to delete"
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
