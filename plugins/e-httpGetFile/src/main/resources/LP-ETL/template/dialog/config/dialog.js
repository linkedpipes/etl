define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-httpGetFile#",
        "$type": "Configuration",
        "uri" : {
            "$type" : "str",
            "$property" : "fileUri",
            "$control": "fileUriControl",
            "$label" : "File URL"
        },
        "fileName" : {
            "$type" : "str",
            "$property" : "fileName",
            "$control": "fileNameControl",
            "$label" : "File name"
        },
        "hardRedirect" : {
            "$type" : "bool",
            "$property" : "hardRedirect",
            "$control": "hardRedirectControl",
            "$label" : "Force to follow redirects"
        },
        "encodeUrl" : {
            "$type" : "bool",
            "$property" : "encodeUrl",
            "$control": "encodeUrlControl",
            "$label" : "Encode input URL"
        },
        "userAgent" : {
            "$type" : "str",
            "$property" : "userAgent",
            "$control": "userAgentControl",
            "$label" : "User agent"
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
