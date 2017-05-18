define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://etl.linkedpipes.com/ontology/components/e-voidDataset/",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "getDistributionIRIFromInput": {
            "$type": "bool",
            "$label": "Get distribution IRI from input"
        },
        "distributionIRI": {
            "$type": "str",
            "$label": "Distribution IRI"
        },
        "exampleResourceIRIs": {
            "$type": "value",
            "$array": true,
            "$label": "Example resource URLs"
        },
        "copyDownloadURLsToDataDumps": {
            "$type": "bool",
            "$label": "Copy download URLs to void:dataDump"
        },
        "sparqlEndpointIRI": {
            "$type": "str",
            "$label": "SPARQL endpoint URL"
        }
    };

    function controller($scope, $service) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

        DESC.exampleResourceIRIs["$onSave"] = $service.v1.fnc.removeEmptyIri;

        const dialogManager = $service.v1.manager(DESC, $scope.dialog);

        $service.onStore = function () {
            dialogManager.save();
        };

        dialogManager.load();

    }

    controller.$inject = ['$scope', '$service'];
    return controller;
});
