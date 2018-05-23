define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/l-solr#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "server": {
            "$type": "str",
            "$label": "URL of Solr instance"
        },
        "core": {
            "$type": "str",
            "$label": "Core name"
        },
        "replace": {
            "$type": "bool",
            "$label": "Replace data"
        },
        "useAuthentication": {
            "$type": "bool",
            "$label": "Use authentication"
        },
        "userName": {
            "$type": "str",
            "$label": "User name"
        },
        "password": {
            "$type": "str",
            "$label": "Password"
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
