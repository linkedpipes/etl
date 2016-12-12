define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-bingTranslator#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "subscriptionKey": {
            "$type": "str",
            "$label": "Subscription key"
        },
        "defaultLanguage": {
            "$type": "str",
            "$label": "Default source language"
        },
        "targetLanguage": {
            "$array": true,
            "$type": "str",
            "$label": "Target language",
            "$onLoad": (value) => {
                return value.join(",");
            },
            "$onSave": (value) => {
                return value.split(",");
            }
        },
        "useBCP47": {
            "$type": "bool",
            "$label": "Use BCP47 extension T"
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
