define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-shaclJena#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "outputShapes": {
            "$type": "bool",
            "$label": "Copy Shapes to output"
        },
        "failOnError": {
            "$type": "bool",
            "$label": "Fail on a rule violation"
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
