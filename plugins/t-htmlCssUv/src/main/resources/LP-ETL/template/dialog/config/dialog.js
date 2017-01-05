define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-htmlCssUv#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "class": {
            "$type": "str",
            "$label": "Class of root subject"
        },
        "predicate": {
            "$type": "str",
            "$label": "Default has predicate"
        },
        "includeSourceInformation": {
            "$type": "bool",
            "$label": "Generate source info"
        },
        "action": {
            "$label": "Actions",
            "$array": true,
            "$object": {
                "$type": "Action",
                "name": {
                    "$type": "str"
                },
                "type": {
                    "$type": "str"
                },
                "data": {
                    "$type": "str"
                },
                "output": {
                    "$type": "str"
                }
            }
        }
    };

    function controller($scope, $service) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

        $scope.onDelete = function (index) {
            $scope.dialog.action.value.splice(index, 1);
        };

        $scope.onAdd = function () {
            $scope.dialog.action.value.push({
                'name': {'value': ''},
                'type': {'value': 'QUERY'},
                'data': {'value': ''},
                'output': {'value': ''}
            });
            //
            console.log('onAdd', $scope.dialog.records);
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
