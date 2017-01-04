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
        "reference": {
            "$label": "To download",
            "$array": true,
            "$object": {
                "$type": "Reference",
                "fileUri" : {
                    "$type": "str"
                },
                "fileName": {
                    "$type": "str"
                }
            }
        },
    };

    function controller($scope, $service) {

        $scope.onAdd = function () {
            $scope.dialog.reference.value.push({
                'fileUri': {'value': ''},
                'fileName': {'value': ''}
            });
        };

        $scope.onDelete = function (index) {
            $scope.dialog.reference.value.splice(index, 1);
        };

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
