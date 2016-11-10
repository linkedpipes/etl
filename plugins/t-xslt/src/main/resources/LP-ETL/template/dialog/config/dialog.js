define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-xslt#",
        "$type": "Configuration",
        "$control": {
            "$predicate": "auto"
        },
        "template": {
            "$type": "str",
            "$label": "XSLT Template"
        },
        "extension": {
            "$type": "str",
            "$label": "Transformed file extension"
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
