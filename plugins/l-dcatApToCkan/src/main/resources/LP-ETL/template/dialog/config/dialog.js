define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/l-dcatApToCkan#",
        "$type": "Configuration",
        "$options" : {
            "$predicate": "auto",
            "$control": "auto"
        },
        "apiUrl" : {
            "$type" : "str",
            "$label" : "CKAN Action API URL"
        },
        "apiKey" : {
            "$type" : "str",
            "$label" : "CKAN API Key"
        },
        "loadToCKAN" : {
            "$type" : "bool",
            "$label" : "Load to CKAN"
        },
        "datasetID" : {
            "$type" : "str",
            "$label" : "CKAN dataset ID"
        },
        "filename" : {
            "$type" : "str",
            "$label" : "Output JSON filename"
        },
        "orgID" : {
            "$type" : "str",
            "$label" : "Dataset owner organization ID"
        },
        "loadLanguage" : {
            "$type" : "str",
            "$label" : "Load language (cs|en)"
        },
        "generateVirtuosoExample" : {
            "$type" : "bool",
            "$label" : "Turtle example resources (for Virtuoso)"
        },
        "generateExampleResource" : {
            "$type" : "bool",
            "$label" : "Example resources"
        },
        "overwrite" : {
            "$type" : "bool",
            "$label" : "Overwrite target CKAN record"
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
