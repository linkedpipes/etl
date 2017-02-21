define([], function () {
    "use strict";

    function listToString(string) {
        return string.join();
    }

    function stringToList(list) {
        if (list === '') {
            return [];
        } else {
            let result = [];
            list.split(',').forEach(function (value) {
                value = value.trim();
                if (value !== '') {
                    result.push(value);
                }
            });
            return result;
        }
    }

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/e-distributionMetadata#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "datasetURI": {
            "$type": "str",
            "$label": "Dataset IRI"
        },
        "distributionURI": {
            "$type": "str",
            "$label": "Distribution IRI"
        },
        "language_orig": {
            "$type": "str",
            "$label": "Original language tag"
        },
        "title_orig": {
            "$type": "str",
            "$label": "Dataset title in original language"
        },
        "title_en": {
            "$type": "str",
            "$label": "Dataset title in English"
        },
        "desc_orig": {
            "$type": "str",
            "$label": "Dataset description in original language"
        },
        "desc_en": {
            "$type": "str",
            "$label": "Dataset description in English"
        },
        "license": {
            "$type": "str",
            "$label": "License IRI"
        },
        "sparqlEndpointUrl": {
            "$type": "str",
            "$label": "SPARQL Endpoint URL"
        },
        "mediaType": {
            "$type": "str",
            "$label": "Media (MIME) type"
        },
        "downloadURL": {
            "$type": "str",
            "$label": "Download URL"
        },
        "accessURL": {
            "$type": "str",
            "$label": "Access URL"
        },
        "modified": {
            "$type": "date",
            "$label": "Modified"
        },
        "issued": {
            "$type": "date",
            "$label": "Issued"
        },
        "temporalEnd": {
            "$type": "date",
            "$label": "Temporal end"
        },
        "temporalStart": {
            "$type": "date",
            "$label": "Temporal start"
        },
        "schema": {
            "$type": "str",
            "$label": "Schema URL"
        },
        "schemaType": {
            "$type": "str",
            "$label": "Schema MIME type"
        },
        "useDatasetURIfromInput": {
            "$type": "bool",
            "$label": "Dataset IRI from dataset"
        },
        "useNow": {
            "$type": "bool",
            "$label": "Use current date as modified"
        },
        "titleFromDataset": {
            "$type": "bool",
            "$label": "Get title from dataset"
        },
        "generateDistroURIFromDataset": {
            "$type": "bool",
            "$label": "Distribution IRI based on dataset"
        },
        "originalLanguageFromDataset": {
            "$type": "bool",
            "$label": "Original language from dataset"
        },
        "issuedFromDataset": {
            "$type": "bool",
            "$label": "Use issued date from dataset"
        },
        "descriptionFromDataset": {
            "$type": "bool",
            "$label": "Get description from dataset"
        },
        "licenseFromDataset": {
            "$type": "bool",
            "$label": "Use license from dataset"
        },
        "schemaFromDataset": {
            "$type": "bool",
            "$label": "Dataset doc as schema"
        },
        "useTemporal": {
            "$type": "bool",
            "$label": "Use temporal coverage"
        },
        "useNowTemporalEnd": {
            "$type": "bool",
            "$label": "Temporal coverage end is Now"
        },
        "temporalFromDataset": {
            "$type": "bool",
            "$label": "Temporal coverage from dataset"
        },
        "exampleResources": {
            "$array": true,
            "$type": "str",
            "$onLoad": listToString,
            "$onSave": stringToList,
            "$label": "Example resources (comma separated)"
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
