define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/e-oauth2Token#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "provider": {
            "$type": "str",
            "$label": "Provider"
        },
        "tenant": {
            "$type": "str",
            "$label": "Tenant ID"
        },
        "clientId": {
            "$type": "str",
            "$label": "Client ID"
        },
        "clientSecret": {
            "$type": "str",
            "$label": "Client secret"
        },
        "scope": {
            "$type": "str",
            "$label": "Scope"
        },
        "tokenEndpoint": {
            "$type": "str",
            "$label": "Token endpoint"
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
