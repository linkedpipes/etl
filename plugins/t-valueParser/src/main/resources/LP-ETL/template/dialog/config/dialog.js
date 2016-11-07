define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-valueParser#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "regexp": {
            "$type": "str",
            "$label": "Regular expression with groups"
        },
        "source": {
            "$type": "str",
            "$label": "Predicate for input values"
        },
        "preserveMetadata": {
            "$type": "bool",
            "$label": "Preserve language tag/datatype"
        },
        "binding": {
            "$label": "Mappings",
            "$array": true,
            "$object": {
                "$type": "Binding",
                "group": {
                    "$type": "str"
                },
                "target": {
                    "$type": "str"
                }
            }
        }
    };

    function controller($scope, $service) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

        $scope.onAdd = function () {
            $scope.dialog.binding.value.push({
                "group": {"value": ""},
                "target": {"value": ""}
            });
        };

        $scope.onDelete = function (index) {
            $scope.dialog.binding.value.splice(index, 1);
        };

        const dialogManager = $service.v1.manager(DESC, $scope.dialog);

        $service.onStore = function () {
            dialogManager.save();
        };

        dialogManager.load();

    }

    controller.$inject = ['$scope', '$service'];
    return controller;
});
