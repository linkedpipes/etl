define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/l-wikibase#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "endpoint": {
            "$type": "str",
            "$label": "Wikibase API Endpoint URL"
        },
        "userName": {
            "$type": "str",
            "$label": "User name"
        },
        "password": {
            "$type": "str",
            "$label": "Password"
        },
      "ontologyIriBase": {
        "$type": "str",
        "$label": "Wikibase ontology IRI prefix (wikibase:)"
      },
      "instanceIriBase": {
        "$type": "str",
        "$label": "Wikibase instance IRI prefix (wd:)"
      },
      "averageTimePerEdit": {
        "$type": "int",
        "$label": "Average time per edit"
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
