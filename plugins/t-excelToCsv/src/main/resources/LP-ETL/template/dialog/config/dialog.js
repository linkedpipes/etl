define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-excelToCsv#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "fileName": {
            "$type": "str",
            "$label": "Output file name<"
        },
        "sheetFilter": {
            "$type": "str",
            "$label": "Sheet filter"
        },
        "rowStart": {
            "$type": "int",
            "$label": "Row start"
        },
        "rowEnd": {
            "$type": "int",
            "$label": "Row end"
        },
        "columnStart": {
            "$type": "int",
            "$label": "Column start"
        },
        "columnEnd": {
            "$type": "int",
            "$label": "Column end"
        },
        "header": {
            "$type": "bool",
            "$label": "Virtual columns with header"
        },
        "numericParse": {
            "$type": "bool",
            "$label": "Determine type for numeric cells"
        },
        "skipEmptyRows": {
            "$type": "bool",
            "$label": "Skip empty rows"
        },
        "includeSheetName": {
            "$type": "bool",
            "$label": "Add sheet name as a column"
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
