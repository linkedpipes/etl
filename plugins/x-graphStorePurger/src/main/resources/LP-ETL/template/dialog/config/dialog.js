define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/x-graphStorePurger#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "useAuthentication": {
            "$type": "bool",
            "$label": "Use authentication"
        },
        "username": {
            "$type": "str",
            "$label": "User name"
        },
        "password": {
            "$type": "str",
            "$label": "Password"
        },
        "endpoint": {
            "$type": "str",
            "$label": "Graph store protocol endpoint"
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
