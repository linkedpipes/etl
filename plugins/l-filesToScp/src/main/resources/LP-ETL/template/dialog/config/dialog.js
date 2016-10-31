define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/l-filesToScp#",
        "$type": "Configuration",
        "$control": {
            "$predicate": "auto"
        },
        "host": {
            "$type": "str",
            "$label": "Host address"
        },
        "port": {
            "$type": "int",
            "$label": "Port number"
        },
        "directory": {
            "$type": "str",
            "$label": "User name"
        },
        "createDirectory": {
            "$type": "bool",
            "$label": "Password"
        },
        "userName": {
            "$type": "str",
            "$label": "Target directory"
        },
        "password": {
            "$type": "str",
            "$label": "Create target directory"
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
