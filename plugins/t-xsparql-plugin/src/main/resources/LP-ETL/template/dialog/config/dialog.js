define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-xsparql#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "template": {
            "$type": "str",
            "$label": "XSPARQL Template"
        },
        "extension": {
            "$type": "str",
            "$label": "Transformed file extension"
        },
        "reference": {
                    "$label": "Bind external variables",
                    "$array": true,
                    "$object": {
                        "$type": "Reference",
                        "key" : {
                            "$type": "str"
                        },
                        "val": {
                            "$type": "str"
                        }
                    }
        },
    };

    function controller($scope, $service) {

        $scope.onAdd = function () {
            $scope.dialog.reference.value.push({
                'key': {'value': ''},
                'val': {'value': ''}
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
