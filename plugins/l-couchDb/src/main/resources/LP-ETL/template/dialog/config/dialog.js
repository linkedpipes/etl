define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/l-couchDb#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "url": {
            "$type": "str",
            "$label": "Server URL"
        },
        "database": {
            "$type": "str",
            "$label": "Database name"
        },
        "clearBeforeLoading": {
            "$type": "bool",
            "$label": "Clear database"
        },
        "batchSize": {
            "$type": "int",
            "$label": "Batch size"
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
