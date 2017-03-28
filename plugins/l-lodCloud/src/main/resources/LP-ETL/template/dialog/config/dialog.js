define([], function () {
    "use strict";

    const DESC = {
        "$namespace" :
            "http://plugins.linkedpipes.com/ontology/l-lodCloud#",
        "$type": "Configuration",
        "$options" : {
            "$predicate": "auto",
            "$control": "auto"
        },
        "apiKey" : {
            "$type" : "str",
            "$label" : "Datahub.io API Key"
        },
        "datasetID" : {
            "$type" : "str",
            "$label" : "CKAN dataset ID"
        },
        "shortname" : {
            "$type" : "str",
            "$label" : "Dataset short name - for LOD cloud circle label"
        },
        "topic" : {
            "$type" : "str",
            "$label" : "LOD Cloud topic"
        },
        "limitedSparql" : {
            "$type": "bool",
            "$label": "Indicates that the SPARQL endpoint does not serve the whole dataset"
        },
        "lodNoLinks" : {
            "$type": "bool",
            "$label": "Data set has no external RDF links to other datasets"
        },
        "lodUnconnected" : {
            "$type": "bool",
            "$label": "Data set has no external RDF links to or from other datasets"
        },
        "lodNeedsInfo" : {
            "$type": "bool",
            "$label": "The data provider or data set homepage do not provide mininum information"
        },
        "lodNeedsFixing" : {
            "$type": "bool",
            "$label": "The dataset is currently broken"
        },
        "versionGenerated" : {
            "$type": "bool",
            "$label": "Generate version as current date"
        },
        "licenseMetadataTag" : {
            "$type": "str",
            "$label": "License metadata tag"
        },
        "provenanceMetadataTag" : {
            "$type": "str",
            "$label": "Provenance metadata tag"
        },
        "publishedTag" : {
            "$type": "str",
            "$label": "Published tag"
        },
        "vocabMappingTag" : {
            "$type": "str",
            "$label": "Vocabulary mapping type tag"
        },
        "vocabTag" : {
            "$type": "str",
            "$label": "Proprietary vocabulary type tag"
        },
        "version" : {
            "$type": "str",
            "$label": "Dataset version"
        },
        "licenseId" : {
            "$type": "str",
            "$label": "CKAN License ID"
        },
        "organizationId" : {
            "$type": "str",
            "$label": "CKAN Organization ID"
        },
        "namespace" : {
            "$type": "str",
            "$label": "RDF namespace"
        },
        "vocabularies" : {
            "$type": "str",
            "$array": true,
            "$onLoad": function (values) {
                return values.map(function (v) {
                    return {"@value": v};
                });
            },
            "$label": "Standard prefixes of vocabularies used"
        },
        "additionalTags" : {
            "$type": "str",
            "$array": true,
            "$onLoad": function (values) {
                return values.map(function (v) {
                    return {"@value": v};
                });
            },
            "$label": "Additional CKAN tags"
        },
        "links" : {
            "$label": "LOD cloud linkage targets",
            "$array": true,
            "$object": {
                "$type": "LinkCount",
                "targetDataset": {
                    "$type": "str"
                },
                "linkCount": {
                    "$type": "int"
                }
            }
        },
        "mappingFiles" : {
            "$label": "Vocabulary mapping files",
            "$array": true,
            "$object": {
                "$type": "MappingFile",
                "mappingFormat": {
                    "$type": "str"
                },
                "mappingFile": {
                    "$type": "str"
                }
            }
        },
        "sparqlEndpointName" : {
            "$type": "str",
            "$label": "SPARQL endpoint name"
        },
        "sparqlEndpointDescription" : {
            "$type": "str",
            "$label": "SPARQL endpoint description"
        }
    };

    const TOPICS = [
        "media", "geographic", "lifesciences", "publications", "government", "ecommerce", "socialweb",
        "usergeneratedcontent", "schemata", "crossdomain"
    ];

    const LICENSES = [
        {
            "notation": "pddl" ,
            "label": "Open Data Commons Public Domain Dedication and License (PDDL)"
        }, {
            "notation": "ccby" ,
            "label": "Creative Commons Attribution"
        }, {
            "notation": "ccbysa" ,
            "label": "Creative Commons Attribution Share-Alike"
        }, {
            "notation": "cczero" ,
            "label": "Creative Commons CCZero"
        }, {
            "notation": "ccnc" ,
            "label": "Creative Commons Non-Commercial (Any)"
        }, {
            "notation": "gfdl" ,
            "label": "GNU Free Documentation License"
        }, {
            "notation": "notspecified" ,
            "label": "License Not Specified"
        }, {
            "notation": "odcby" ,
            "label": "Open Data Commons Attribution License"
        }, {
            "notation": "odcodbl" ,
            "label": "Open Data Commons Open Database License (ODbL)"
        }, {
            "notation": "otherat" ,
            "label": "Other (Attribution)"
        }, {
            "notation": "othernc" ,
            "label": "Other (Non-Commercial)"
        }, {
            "notation": "otherclosed" ,
            "label": "Other (Not Open)"
        }, {
            "notation": "otheropen" ,
            "label": "Other (Open)"
        }, {
            "notation": "otherpd" ,
            "label": "Other (Public Domain)"
        }, {
            "notation": "ukogl" ,
            "label": "UK Open Government Licence (OGL)"
        }
    ];

    const PUBLISHEDTAGS = [{
        "notation": "PublishedByProducer",
        "label": "Published by producer"
    }, {
        "notation": "PublishedByThirdParty",
        "label": "Published by third party"
    }
    ];

    const LICENSEMETADATA = [{
        "notation": "LicenseMetadata",
        "label": "Contains license metadata"
    }, {
        "notation": "NoLicenseMetadata",
        "label": "No license metadata"
    }
    ];

    const PROVENANCETAGS = [{
        "notation": "ProvenanceMetadata",
        "label": "Contains provenance metadata"
    }, {
        "notation": "NoProvenanceMetadata",
        "label": "No provenance metadata"
    }
    ];

    const VOCABMAPPINGSTAGS = [{
        "notation": "VocabMappings",
        "label": "Contains vocabulary mappings"
    }, {
        "notation": "NoVocabMappings",
        "label": "No vocabulary mappings"
    }
    ];

    const VOCABTAGS = [{
        "notation": "NoProprietaryVocab",
        "label": "No proprietary vocabulary"
    }, {
        "notation": "DerefVocab",
        "label": "Dereferencable vocabulary"
    }, {
        "notation": "NoDerefVocab",
        "label": "Non-dereferencable vocabulary"
    }
    ];

    const MAPPINGFORMATS = [
        "RDFS", "OWL", "SKOS", "R2R", "RIF"
    ];

    function controller($scope, $service) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

        $scope.onDeleteLinkCount = function (index) {
            $scope.dialog.links.value.splice(index, 1);
        };

        $scope.onAddLinkCount = function () {
            $scope.dialog.links.value.push({
                'targetDataset': {'value': ''},
                'linkCount': {'value': '0'}
            });
            //
            console.log('onAddLinkCount', $scope.dialog.records);
        };

        $scope.onDeleteMappingFile = function (index) {
            $scope.dialog.mappingFiles.value.splice(index, 1);
        };

        $scope.onAddMappingFile = function () {
            $scope.dialog.mappingFiles.value.push({
                'mappingFormat': {'value': 'RDFS'},
                'mappingFile': {'value': ''}
            });
            //
            console.log('onAddMappingFile', $scope.dialog.records);
        };

        const dialogManager = $service.v1.manager(DESC, $scope.dialog);

        $service.onStore = function () {
            dialogManager.save();
        };

        $scope.transformChip = function (chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {"@value": chip};
        };

        $scope.topics = TOPICS;
        $scope.licenses = LICENSES;
        $scope.licensemetadatatags = LICENSEMETADATA;
        $scope.publishedtags = PUBLISHEDTAGS;
        $scope.provenancetags = PROVENANCETAGS;
        $scope.vocabmappingstags = VOCABMAPPINGSTAGS;
        $scope.vocabtags = VOCABTAGS;
        $scope.mappingformats = MAPPINGFORMATS;


        dialogManager.load();

    }

    controller.$inject = ['$scope', '$service'];
    return controller;
});