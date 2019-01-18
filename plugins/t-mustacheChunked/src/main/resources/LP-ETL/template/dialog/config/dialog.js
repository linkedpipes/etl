define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-mustacheChunked#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "class": {
            "$type": "str",
            "$label": "Entity class IRI"
        },
        "template": {
            "$type": "str",
            "$label": "Mustache template"
        },
        "addFirstFlag" : {
            "$type" : "bool",
            "$label" : "Add predicate for first items"
        },
        "escapeForJson" : {
            "$type" : "bool",
            "$label" : "Escape values for JSON output."
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
