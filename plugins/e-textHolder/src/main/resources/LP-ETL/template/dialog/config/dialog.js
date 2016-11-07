define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-textHolder#",
        "$type": "Configuration",
        "fileName" : {
            "$type" : "str",
            "$property" : "fileName",
            "$control": "fileNameControl",
            "$label" : "File name"
        },
        "content" : {
            "$type" : "str",
            "$property" : "content",
            "$control": "contentControl",
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
