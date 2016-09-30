define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/x-virtuoso#",
        "$type": "Configuration",
        "uri" : {
            "$type" : "str",
            "$property" : "uri",
            "$control": "uriControl",
            "$label" : "Virtuoso JDBC connection string"
        },
        "fileName" : {
            "$type" : "str",
            "$property" : "fileName",
            "$control": "fileNameControl",
            "$label" : "Filename to load"
        },
        "graph" : {
            "$type" : "str",
            "$property" : "graph",
            "$control": "graphControl",
            "$label" : "Target graph IRI"
        },
        "directory" : {
            "$type" : "str",
            "$property" : "directory",
            "$control": "directoryControl",
            "$label" : "Remote directory with source files"
        },
        "clearGraph" : {
            "$type" : "bool",
            "$property" : "clearGraph",
            "$control": "clearGraphControl",
            "$label" : "Clear target graph"
        },
        "clearLoadList" : {
            "$type" : "bool",
            "$property" : "clearSqlLoadTable",
            "$control": "clearSqlLoadTableControl",
            "$label" : "Clear Virtuoso load list"
        },
        "username" : {
            "$type" : "str",
            "$property" : "username",
            "$control": "usernameControl",
            "$label" : "Virtuoso user name"
        },
        "password" : {
            "$type" : "str",
            "$property" : "password",
            "$control": "passwordControl",
            "$label" : "Virtuoso password"
        },
        "updateInterval" : {
            "$type" : "int",
            "$property" : "updateInterval",
            "$control": "updateIntervalControl",
            "$label" : "Status update interval"
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
