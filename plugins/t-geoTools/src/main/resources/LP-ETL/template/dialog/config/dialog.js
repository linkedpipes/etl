define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/t-geoTools#",
        "$type": "Configuration",
        "$control" : {
            "$predicate": "auto"
        },
        "type": {
            "$type": "iri",
            "$label" : "Input resource type"
        },
        "coord": {
            "$type": "iri",
            "$label" : "Predicate with coordinates"
        },
        "coordType": {
            "$type": "iri",
            "$label" : "Predicate with coordinates type"
        },
        "defaultCoordType": {
            "$type": "str",
            "$label" : "Default coordinate type"
        },
        "outputPredicate": {
            "$type": "iri",
            "$label" : "Predicate used to connect with new entity"
        },
        "outputCoordType": {
            "$type": "str",
            "$label" : "Type of output coordinates"
        },
        "failOnError": {
            "$type": "bool",
            "$label" : "Fail on error"
        },
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
