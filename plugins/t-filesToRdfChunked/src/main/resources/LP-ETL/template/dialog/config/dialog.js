define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/t-filesToRdf#",
        "$type": "Configuration",
        "chunkSize" : {
            "$type" : "int",
            "$property" : "chunkSize",
            "$control": "chunkSizeControl",
            "$label" : "Files per chunk"
        },
        "mimeType" : {
            "$type" : "str",
            "$property" : "mimeType",
            "$control": "mimeTypeControl",
            "$label" : "Format"
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
