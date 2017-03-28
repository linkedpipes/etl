define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-geoTools#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "type": {
            "$type": "iri",
            "$label": "Input resource type"
        },
        "coord": {
            "$type": "iri",
            "$label": "Predicate for coordinates"
        },
        "coordType": {
            "$type": "iri",
            "$label": "Predicate for coordinate reference system"
        },
        "defaultCoordType": {
            "$type": "str",
            "$label": "Default coordinate reference system"
        },
        "outputPredicate": {
            "$type": "iri",
            "$label": "Predicate for linking to newly created points"
        },
        "outputCoordType": {
            "$type": "str",
            "$label": "Output coordinate reference system"
        },
        "failOnError": {
            "$type": "bool",
            "$label": "Fail on error"
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
