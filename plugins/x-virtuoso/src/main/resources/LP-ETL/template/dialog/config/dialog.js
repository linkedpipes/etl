define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/x-virtuoso#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "uri": {
            "$type": "str",
            "$label": "Virtuoso JDBC connection string"
        },
        "fileName": {
            "$type": "str",
            "$label": "Filename to load"
        },
        "graph": {
            "$type": "iri",
            "$label": "Target graph IRI"
        },
        "directory": {
            "$type": "str",
            "$label": "Remote directory with source files"
        },
        "clearGraph": {
            "$type": "bool",
            "$label": "Clear target graph"
        },
        "username": {
            "$type": "str",
            "$label": "Virtuoso user name"
        },
        "password": {
            "$type": "str",
            "$label": "Virtuoso password"
        },
        "updateInterval": {
            "$type": "int",
            "$label": "Status update interval"
        },
        "loaderCount": {
            "$type": "int",
            "$label": "Number of loaders to use"
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
