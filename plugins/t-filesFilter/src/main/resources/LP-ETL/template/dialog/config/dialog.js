define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/t-filesFilter#",
        "$type": "Configuration",
        "fileNamePattern" : {
            "$type" : "str",
            "$property" : "fileNamePattern",
            "$control": "fileNamePatternControl",
            "$label" : "File name filter pattern<"
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
