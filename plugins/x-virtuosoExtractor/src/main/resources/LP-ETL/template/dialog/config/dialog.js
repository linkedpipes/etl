define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/x-virtuosoExtractor#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "host" : {
            "$type" : "str",
            "$label" : "Virtuoso JDBC connection string"
        },
        "userName" : {
            "$type" : "str",
            "$label" : "Virtuoso user name"
        },
        "password" : {
            "$type" : "str",
            "$label" : "Virtuoso password"
        },
        "outputPath" : {
            "$type" : "str",
            "$label" : "Output path"
        },
        "graph" : {
            "$type" : "iri",
            "$label" : "Source graph IRI"
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
