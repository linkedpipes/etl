define([], function () {
    "use strict";

    function directiveFunction($location) {
        return {
            "restrict": "E",
            "scope": {
                "ngModel": "=",
                "isUsed": "="
            },
            "template": require("./template-usage-tab.html"),
            "link": ($scope) => {

                $scope.loaded = false;
                $scope.$watch("ngModel", (usage) => {
                    if (usage === undefined) {
                        $scope.data = {};
                        return;
                    }
                    $scope.loaded = true;
                    $scope.data = usage;
                    $scope.empty = Object.keys(usage).length === 0;
                });

                $scope.onPipeline = (iri) => {
                    $location.path("/pipelines/edit/canvas").search({
                        "pipeline": iri
                    });
                };

            }
        };
    }

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        //
        app.directive("lpTemplateUsageTab", ["$location",  directiveFunction]);
    };

});
