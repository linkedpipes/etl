define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-sparqlEndpoint#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "query" : {
            "$type" : "str",
            "$label" : "SPARQL CONSTRUCT query"
        },
        "endpoint" : {
            "$type" : "str",
            "$label" : "Endpoint URL"
        },
        "defaultGraph" : {
            "$type": "value",
            "$array": true,
            "$label" : "Default graph"
        },
        "headerAccept" : {
            "$type" : "str",
            "$label" : "Used MimeType"
        },
        "encodeRdf": {
            "$type": "bool",
            "$label": "Encode RDF"
        },
        "useAuthentication": {
            "$type": "bool",
            "$label": "Use authentication"
        },
        "userName": {
            "$type": "str",
            "$label": "User name"
        },
        "password": {
            "$type": "str",
            "$label": "Password"
        },
        "useTolerantRepository": {
            "$type": "bool",
            "$label": "Fix invalid types"
        },
        "handleInvalidData": {
            "$type": "bool",
            "$label": "Handle invalid data"
        }
    };

    const MIME_TYPES = [
        "application/rdf+xml",
        "application/xml",
        "application/n-triples",
        "application/trix",
        "application/trig",
        "application/n-quads",
        "application/ld+json",
        "application/rdf+json",
        "application/xhtml+xml",
        "application/html",
        "text/xml",
        "text/n3",
        "text/plain",
        "text/turtle",
        "text/rdf+n3",
        "text/nquads",
        "text/html"
    ];

    function controller($scope, $service) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

        DESC.defaultGraph["$onSave"] = $service.v1.fnc.removeEmptyIri;

        const dialogManager = $service.v1.manager(DESC, $scope.dialog);

        $service.onStore = function () {
            dialogManager.save();
        };

        $scope.getMimeTypes = function (query) {
            return query ? MIME_TYPES.filter(createFilter(query)) : MIME_TYPES;
        };

        function createFilter(query) {
            query = angular.lowercase(query);
            return (item) => (item.indexOf(query) !== -1);
        }

        dialogManager.load();
        console.log("onLoad", $scope.dialog);
    }

    controller.$inject = ['$scope', '$service'];
    return controller;
});
