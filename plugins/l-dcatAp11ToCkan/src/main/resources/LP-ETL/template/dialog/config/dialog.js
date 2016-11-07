define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/l-dcatAp11ToCkan#",
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
        "createCkanOrg" : {
            "$type" : "bool",
            "$label" : "Create/Use CKAN organization from dataset publisher"
        },
        "datasetID" : {
            "$type" : "str",
            "$label" : "CKAN dataset ID"
        },
        "organizationId" : {
            "$type" : "str",
            "$label" : "Dataset owner organization ID"
        },
        "loadLanguage" : {
            "$type" : "str",
            "$label" : "Load language (cs|en)"
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
