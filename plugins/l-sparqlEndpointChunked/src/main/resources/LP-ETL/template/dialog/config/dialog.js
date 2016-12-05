define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/l-sparqlEndpoint#",
        "$type": "Configuration",
        "$control": {
            "$predicate": "auto"
        },
        "endpoint": {
            "$type": "str",
            "$label": "Endpoint"
        },
        "targetGraphURI": {
            "$type": "str",
            "$label": "Target graph IRI"
        },
        "clearGraph": {
            "$type": "bool",
            "$label": "Clear target graph before loading"
        },
        "commitSize": {
            "$type": "int",
            "$label": "Commit size"
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
