define([], function () {
    "use strict";

    function directiveFunction($location, templateService) {
        return {
            "restrict": "E",
            "scope": {
                "ngModel": "="
            },
            "templateUrl":
                "app/components/templates/usageTab/templateUsageTab.html",
            "link": (scope) => {

                scope.$watch("ngModel", (template) => {
                    if (template === undefined) {
                        scope.template = {};
                        return;
                    }
                    // Update data.
                    scope.template = template;
                    templateService.getUsage(template.id).then((usage) => {
                        scope.data = usage;
                    });
                });

                scope.onPipeline = (iri) => {
                    $location.path('/pipelines/edit/canvas').search({
                        'pipeline': iri
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
        app.directive("lpTemplateUsageTab",
            ["$location", "template.service", directiveFunction]);
    };

});
