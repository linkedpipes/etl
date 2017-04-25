define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-XMLtoChunks#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "chunk_size": {
            "$type": "int",
            "$label": "Approximate size per chunk"
        },
        "reference": {
            "$label": "Nodes for which the data shouldn't be split",
            "$array": true,
            "$object": {
                "$type": "Reference",
                "prefix" : {
                    "$type": "str"
                },
                "local" : {
                    "$type": "str"
                }
            }
        }
    };

    function controller($scope, $service) {

        $scope.onAdd = function () {
            $scope.dialog.reference.value.push({
                'prefix': {'value': ''},
                'local': {'value': ''}
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
