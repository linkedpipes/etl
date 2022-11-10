define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-textHolder#",
        "$type": "Configuration",
        "$options" : {
            "$predicate": "auto",
            "$control": "auto"
        },
        "fileName" : {
            "$type" : "str",
            "$label" : "File name"
        },
        "content" : {
            "$type" : "str",
            "$label" : "Content"
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
