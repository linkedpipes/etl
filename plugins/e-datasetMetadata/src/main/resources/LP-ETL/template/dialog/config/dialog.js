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
        "$namespace": "http://plugins.linkedpipes.com/ontology/e-datasetMetadata#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "datasetURI": {
            "$type": "str",
            "$label": "Dataset IRI"
        },
        "language_orig": {
            "$type": "str",
            "$label": "Original language tag"
        },
        "title_cs": {
            "$type": "str",
            "$label": "Dataset title original language"
        },
        "title_en": {
            "$type": "str",
            "$label": "Dataset title in English"
        },
        "desc_cs": {
            "$type": "str",
            "$label": "Description in original language"
        },
        "desc_en": {
            "$type": "str",
            "$label": "Description in English"
        },
        "authors": {
            "$array": true,
            "$type": "str",
            "$onLoad": listToString,
            "$onSave": stringToList,
            "$label": "Authors IRIs"
        },
        "publisherURI": {
            "$type": "str",
            "$label": "Publisher IRI"
        },
        "publisherName": {
            "$type": "str",
            "$label": "Publisher name"
        },
        "license": {
            "$type": "str",
            "$label": "Licenses IRIs"
        },
        "sources": {
            "$array": true,
            "$type": "str",
            "$onLoad": listToString,
            "$onSave": stringToList,
            "$label": "Sources IRIs"
        },
        "languages": {
            "$array": true,
            "$type": "str",
            "$onLoad": listToString,
            "$onSave": stringToList,
            "$label": "Languages IRIs"
        },
        "keywords_orig": {
            "$array": true,
            "$type": "str",
            "$onLoad": listToString,
            "$onSave": stringToList,
            "$label": "Keywords in original language"
        },
        "keywords_en": {
            "$array": true,
            "$type": "str",
            "$onLoad": listToString,
            "$onSave": stringToList,
            "$label": "Keywords in English"
        },
        "themes": {
            "$array": true,
            "$type": "str",
            "$onLoad": listToString,
            "$onSave": stringToList,
            "$label": "Themes IRIs"
        },
        "contactPoint": {
            "$type": "str",
            "$label": "Contact point name"
        },
        "contactPointName": {
            "$type": "str",
            "$label": "Contact point name"
        },
        "periodicity": {
            "$type": "str",
            "$label": "Periodicity"
        },
        "useNow": {
            "$type": "bool",
            "$label": "Modified is Now"
        },
        "useNowTemporalEnd": {
            "$type": "bool",
            "$label": "End is Now"
        },
        "useTemporal": {
            "$type": "bool",
            "$label": "Use temporal coverage"
        },
        "modified": {
            "$type": "date",
            "$label": "Modified is Now"
        },
        "issued": {
            "$type": "date",
            "$label": "Issued"
        },
        "identifier": {
            "$type": "str",
            "$label": "Identifier"
        },
        "landingPage": {
            "$type": "str",
            "$label": "Landing page URL"
        },
        "temporalEnd": {
            "$type": "date",
            "$label": "Temporal end"
        },
        "temporalStart": {
            "$type": "date",
            "$label": "Temporal start"
        },
        "spatial": {
            "$type": "str",
            "$label": "Spatial coverage IRI"
        },
        "schema": {
            "$type": "str",
            "$label": "Human readable documentation URL"
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
