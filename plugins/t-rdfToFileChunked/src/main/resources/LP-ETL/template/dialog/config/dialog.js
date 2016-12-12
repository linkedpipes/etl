define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/t-rdfToFile#",
        "$type": "Configuration",
        "fileName" : {
            "$type" : "str",
            "$property" : "fileName",
            "$control": "fileNameControl",
            "$label" : "Output file name"
        },
        "fileType" : {
            "$type" : "str",
            "$property" : "fileType",
            "$control": "fileTypeControl",
            "$label" : "Format"
        },
        "graphUri" : {
            "$type" : "iri",
            "$property" : "graphUri",
            "$control": "graphUriControl",
            "$label" : "URI of output graph"
        },
        "prefixTurtle" : {
            "$type" : "str",
            "$property" : "prefixTurtle",
            "$control" : "prefixTurtleControl",
            "$label": "Used prefixes as Turtle"
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
