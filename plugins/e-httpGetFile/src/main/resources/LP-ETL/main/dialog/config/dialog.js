define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-httpGetFile#",
        "$type": "Configuration",
        "$options" : {
            "$predicate": "auto",
            "$control": "auto"
        },
        "fileUri" : {
            "$type" : "str",
            "$label" : "File URL"
        },
        "fileName" : {
            "$type" : "str",
            "$label" : "File name"
        },
        "hardRedirect" : {
            "$type" : "bool",
            "$label" : "Force to follow redirects"
        },
        "encodeUrl" : {
            "$type" : "bool",
            "$label" : "Encode input URL"
        },
        "userAgent" : {
            "$type" : "str",
            "$label" : "User agent"
        },
        "utf8Redirect": {
          "$type" : "bool",
          "$label" : "Use UTF-8 encoding for redirect"
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
