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
            "$label": "Output file template"
        },
        "sheetFilter": {
            "$type": "str",
            "$label": "Sheet filter"
        },
        "rowStart": {
            "$type": "int",
            "$label": "Row start",
            "$onLoad": increase,
            "$onSave": decrease
        },
        "rowEnd": {
            "$type": "int",
            "$label": "Row end",
            "$onLoad": increase,
            "$onSave": decrease
        },
        "columnStart": {
            "$type": "int",
            "$label": "Column start",
            "$onLoad": increase,
            "$onSave": decrease
        },
        "columnEnd": {
            "$type": "int",
            "$label": "Column end",
            "$onLoad": increase,
            "$onSave": decrease
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
        },
        "evalFormula": {
            "$type": "bool",
            "$label": "Evaluate formulas"
        }
    };

    function increase(value) {
        if (value === undefined || value === null) {
            return undefined;
        } else {
            return value + 1;
        }
    }

    function decrease(value) {
        if (value === undefined || value === null) {
            return undefined;
        } else {
            return value - 1;
        }
    }

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
