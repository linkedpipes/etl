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
        "siteIri": {
            "$type": "str",
            "$label": "Wikibase instance IRI"
        },
        "sparqlUrl": {
            "$type": "str",
            "$label": "Wikibase SPARQL URL"
        },
        "referenceProperty": {
            "$type": "str",
            "$label": "Property from the Wikibase"
        },
        "averageTimePerEdit": {
            "$type": "int",
            "$label": "Average time per edit"
        },
        "strictMatching": {
            "$type": "bool",
            "$label": "Strict matching"
        },
        "skipOnError": {
            "$type": "bool",
            "$label": "Skip on error"
        },
        "newItemMessage": {
            "$type": "str",
            "$label": "New item message"
        },
        "replaceItemMessage": {
            "$type": "str",
            "$label": "Replace item message"
        },
        "updateItemMessage": {
            "$type": "str",
            "$label": "Update item message"
        },
        "updateByMergeMessage": {
            "$type": "str",
            "$label": "Update by replace message"
        },
        "retryCount": {
            "$type": "int",
            "$label": "Retry count"
        },
        "retryPause": {
            "$type": "int",
            "$label": "Retry pause"
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
