define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/x-pipeline#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "instance": {
            "$type": "iri",
            "$label": "Instance URL"
        },
        "pipeline": {
            "$type": "iri",
            "$label": "Pipeline ULR"
        },
        "saveDebugData": {
            "$type": "bool",
            "$label": "Save debug data"
        },
        "deleteWorkingData": {
            "$type": "bool",
            "$label": "Delete working directory"
        },
        "logPolicy": {
            "$type": "iri",
            "$label": "Log policy"
        },
        "logLevel": {
            "$type": "str",
            "$label": "Log level"
        },
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
